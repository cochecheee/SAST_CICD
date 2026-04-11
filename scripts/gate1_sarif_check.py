#!/usr/bin/env python3
"""
Gate 1 — SARIF Threshold Check
================================
Reads every *.sarif file under --sarif-dir, counts findings by severity, then:

  CRITICAL > 0   → exit 1  (BLOCK pipeline)
  HIGH     > 5   → exit 2  (WARNING; add --fail-on-high to also block)

Writes a JSON summary to --output (default: gate1-result.json) that the
notify.py script and the dashboard consume.

Usage:
  python scripts/gate1_sarif_check.py --sarif-dir reports/ [--fail-on-high] [--output path]
"""

import argparse
import json
import sys
from pathlib import Path
from typing import List, Dict, Tuple

# ── Thresholds ─────────────────────────────────────────────────────────────
CRITICAL_THRESHOLD = 0   # any CRITICAL → block
HIGH_THRESHOLD = 5       # more than 5 HIGH → warning (or block with --fail-on-high)

# ── SARIF level → internal severity ────────────────────────────────────────
_LEVEL_MAP = {
    "error":    "CRITICAL",
    "critical": "CRITICAL",
    "warning":  "HIGH",
    "high":     "HIGH",
    "note":     "MEDIUM",
    "medium":   "MEDIUM",
    "low":      "LOW",
    "none":     "INFO",
    "info":     "INFO",
}

SEVERITY_ORDER = ["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"]


def _map_level(level: str) -> str:
    return _LEVEL_MAP.get((level or "").strip().lower(), "MEDIUM")


# ── SARIF parsing ───────────────────────────────────────────────────────────

def _parse_sarif(path: Path) -> List[Dict]:
    """Extract normalised findings from a single SARIF v2.1 file."""
    findings = []
    try:
        with open(path, encoding="utf-8") as f:
            sarif = json.load(f)
    except (json.JSONDecodeError, OSError) as exc:
        print(f"  WARNING: cannot parse {path.name}: {exc}", file=sys.stderr)
        return findings

    for run in sarif.get("runs", []):
        tool_name = (
            run.get("tool", {})
               .get("driver", {})
               .get("name", "unknown")
        )
        # Build rule-id → default level index for fallback
        rules_index: Dict[str, str] = {}
        for rule in run.get("tool", {}).get("driver", {}).get("rules", []):
            lvl = (
                rule.get("defaultConfiguration", {})
                    .get("level", "warning")
            )
            rules_index[rule.get("id", "")] = lvl

        for result in run.get("results", []):
            rule_id = result.get("ruleId", "")

            # Prefer explicit level on result, fall back to rule default
            level = result.get("level") or rules_index.get(rule_id, "warning")
            severity = _map_level(level)

            # Location
            locs = result.get("locations", [])
            loc_str = ""
            if locs:
                pl = locs[0].get("physicalLocation", {})
                uri = pl.get("artifactLocation", {}).get("uri", "")
                line = pl.get("region", {}).get("startLine", 0)
                loc_str = f"{uri}:{line}" if uri else ""

            findings.append({
                "tool":       tool_name,
                "rule_id":    rule_id,
                "severity":   severity,
                "message":    result.get("message", {}).get("text", ""),
                "location":   loc_str,
                "sarif_file": path.name,
            })

    return findings


def collect_findings(sarif_dir: str) -> Tuple[List[Path], List[Dict]]:
    root = Path(sarif_dir)
    if not root.exists():
        print(f"WARNING: SARIF directory '{sarif_dir}' does not exist.", file=sys.stderr)
        return [], []

    sarif_files = sorted(root.rglob("*.sarif"))
    print(f"\nFound {len(sarif_files)} SARIF file(s) in '{sarif_dir}':")
    all_findings: List[Dict] = []
    for sf in sarif_files:
        findings = _parse_sarif(sf)
        print(f"  {sf.relative_to(root)}: {len(findings)} finding(s)")
        all_findings.extend(findings)

    return sarif_files, all_findings


# ── Gate evaluation ─────────────────────────────────────────────────────────

def evaluate(findings: List[Dict], fail_on_high: bool) -> Tuple[bool, str, str]:
    """
    Returns (passed: bool, decision: str, message: str).
    decision is one of: PASSED | WARNING | BLOCKED
    """
    counts = {sev: 0 for sev in SEVERITY_ORDER}
    for f in findings:
        sev = f["severity"]
        if sev in counts:
            counts[sev] += 1

    print("\n┌─ Gate 1 — SARIF Threshold Check ─────────────────────────┐")
    for sev in SEVERITY_ORDER:
        bar = "█" * min(counts[sev], 40)
        print(f"│  {sev:<10} {counts[sev]:>5}  {bar}")
    print(f"│  {'TOTAL':<10} {sum(counts.values()):>5}")
    print("└──────────────────────────────────────────────────────────┘")

    # CRITICAL block
    if counts["CRITICAL"] > CRITICAL_THRESHOLD:
        msg = (
            f"GATE 1 BLOCKED — {counts['CRITICAL']} CRITICAL finding(s) detected "
            f"(threshold: {CRITICAL_THRESHOLD}). Pipeline cannot continue."
        )
        return False, "BLOCKED", msg

    # HIGH warn/block
    if counts["HIGH"] > HIGH_THRESHOLD:
        base = f"{counts['HIGH']} HIGH finding(s) detected (threshold: {HIGH_THRESHOLD})."
        if fail_on_high:
            msg = f"GATE 1 BLOCKED — {base} --fail-on-high is set."
            return False, "BLOCKED", msg
        msg = f"GATE 1 WARNING — {base} Pipeline continues."
        print(f"\n⚠  {msg}")
        return True, "WARNING", msg

    return True, "PASSED", "GATE 1 PASSED — no blocking findings."


# ── Entry point ─────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Gate 1: enforce SARIF severity thresholds before SonarCloud."
    )
    parser.add_argument("--sarif-dir",   default="reports",
                        help="Root directory containing *.sarif files (searched recursively)")
    parser.add_argument("--fail-on-high", action="store_true",
                        help="Treat HIGH-threshold breach as a blocking failure")
    parser.add_argument("--output",       default="gate1-result.json",
                        help="Path to write the JSON summary (consumed by notify.py)")
    args = parser.parse_args()

    _, all_findings = collect_findings(args.sarif_dir)
    passed, decision, message = evaluate(all_findings, args.fail_on_high)

    by_severity = {
        sev: sum(1 for f in all_findings if f["severity"] == sev)
        for sev in SEVERITY_ORDER
    }

    summary = {
        "gate":             "Gate 1 — SARIF Threshold",
        "passed":           passed,
        "decision":         decision,
        "message":          message,
        "total_findings":   len(all_findings),
        "by_severity":      by_severity,
        "critical_findings": [
            f for f in all_findings if f["severity"] == "CRITICAL"
        ],
        "high_findings": [
            f for f in all_findings if f["severity"] == "HIGH"
        ][:20],   # cap at 20 to keep the artifact small
    }

    with open(args.output, "w", encoding="utf-8") as out:
        json.dump(summary, out, indent=2)
    print(f"\nSummary written → {args.output}")

    if passed:
        print(f"\n✓  {message}\n")
        sys.exit(0)
    else:
        print(f"\n✗  {message}\n", file=sys.stderr)
        print("Critical findings:", file=sys.stderr)
        for f in summary["critical_findings"]:
            print(
                f"  [{f['tool']}] {f['rule_id']}  @  {f['location']}\n"
                f"    {f['message'][:160]}",
                file=sys.stderr,
            )
        sys.exit(1)


if __name__ == "__main__":
    main()
