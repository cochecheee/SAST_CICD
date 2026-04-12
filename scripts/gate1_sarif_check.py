#!/usr/bin/env python3
"""
Gate 1 — SARIF Threshold Check
================================
Reads every *.sarif file under --sarif-dir, counts findings by severity, then:
Evaluates against security-policy.yml or defaults.

Writes a JSON summary to --output (default: gate1-result.json) that the
notify.py script and the dashboard consume.

Usage:
  python scripts/gate1_sarif_check.py --sarif-dir reports/ [--policy security-policy.yml] [--output path]
"""

import argparse
import json
import sys
import yaml
from pathlib import Path
from typing import List, Dict, Tuple, Any

# ── Default Thresholds (if no policy provided) ───────────────────────────
DEFAULT_POLICIES = {
    "CRITICAL": 0,
    "HIGH": 5,
    "MEDIUM": 10,
    "LOW": 50
}

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


# ── Policy Loading ──────────────────────────────────────────────────────────

def load_policy(policy_path: str) -> Dict[str, Any]:
    if not policy_path or not Path(policy_path).exists():
        return {"gate_rules": []}
    
    try:
        with open(policy_path, "r", encoding="utf-8") as f:
            return yaml.safe_load(f).get("security_policy", {})
    except Exception as e:
        print(f"WARNING: Failed to load policy file {policy_path}: {e}", file=sys.stderr)
        return {"gate_rules": []}


# ── Gate evaluation ─────────────────────────────────────────────────────────

def evaluate(findings: List[Dict], policy: Dict[str, Any]) -> Tuple[bool, str, str, List[str]]:
    """
    Returns (passed: bool, decision: str, message: str, violations: List[str]).
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

    violations = []
    is_blocked = False
    is_warning = False

    gate_rules = policy.get("gate_rules", [])
    
    # If no policy rules, use defaults
    if not gate_rules:
        for sev, threshold in DEFAULT_POLICIES.items():
            if counts[sev] > threshold:
                msg = f"{sev} threshold breach: {counts[sev]} > {threshold}"
                violations.append(msg)
                if sev in ["CRITICAL", "HIGH"]:
                    is_blocked = True
                else:
                    is_warning = True
    else:
        for rule in gate_rules:
            sev = rule.get("severity")
            threshold = rule.get("threshold", 0)
            action = rule.get("action", "BLOCK")
            
            if sev and sev in counts:
                if counts[sev] > threshold:
                    msg = f"{rule.get('name')}: {counts[sev]} {sev} finding(s) detected (threshold: {threshold})"
                    violations.append(msg)
                    if action == "BLOCK":
                        is_blocked = True
                    else:
                        is_warning = True

    if is_blocked:
        return False, "BLOCKED", f"GATE 1 BLOCKED — {', '.join(violations)}", violations
    if is_warning:
        return True, "WARNING", f"GATE 1 WARNING — {', '.join(violations)}", violations

    return True, "PASSED", "GATE 1 PASSED — no blocking findings.", []


# ── Entry point ─────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Gate 1: enforce SARIF severity thresholds before SonarCloud."
    )
    parser.add_argument("--sarif-dir",   default="reports",
                        help="Root directory containing *.sarif files (searched recursively)")
    parser.add_argument("--policy",      default=None,
                        help="Path to security-policy.yml")
    parser.add_argument("--output",       default="gate1-result.json",
                        help="Path to write the JSON summary (consumed by notify.py)")
    args = parser.parse_args()

    _, all_findings = collect_findings(args.sarif_dir)
    policy = load_policy(args.policy)
    passed, decision, message, violations = evaluate(all_findings, policy)

    by_severity = {
        sev: sum(1 for f in all_findings if f["severity"] == sev)
        for sev in SEVERITY_ORDER
    }

    summary = {
        "gate":             "Gate 1 — SARIF Threshold",
        "passed":           passed,
        "decision":         decision,
        "message":          message,
        "violations":       violations,
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
        sys.exit(1)


if __name__ == "__main__":
    main()
