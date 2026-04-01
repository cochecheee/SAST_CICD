"""
Convert an OWASP Dependency-Check JSON report to SARIF 2.1.0.
Usage: python scripts/depcheck_to_sarif.py --input <report.json> --output <out.sarif>
"""
import argparse
import json
import sys
from pathlib import Path

SEVERITY_TO_LEVEL = {
    "CRITICAL": "error",
    "HIGH":     "error",
    "MEDIUM":   "warning",
    "LOW":      "note",
    "INFO":     "note",
}


def cvss_score(vuln: dict) -> float:
    for key in ("cvssv3", "cvssv2"):
        block = vuln.get(key)
        if block:
            return float(block.get("baseScore", 0.0))
    return 0.0


def convert(input_path: str, output_path: str) -> None:
    with open(input_path, encoding="utf-8") as f:
        report = json.load(f)

    rules: dict[str, dict] = {}   # ruleId -> rule object
    results: list[dict] = []

    for dep in report.get("dependencies", []):
        vulns = dep.get("vulnerabilities", [])
        if not vulns:
            continue

        # Prefer relative path; fall back to fileName
        file_path = dep.get("filePath", dep.get("fileName", "unknown"))
        try:
            file_path = str(Path(file_path).as_posix())
        except Exception:
            pass

        for vuln in vulns:
            cve = vuln.get("name", "UNKNOWN")
            severity = vuln.get("severity", "MEDIUM").upper()
            level = SEVERITY_TO_LEVEL.get(severity, "warning")
            description = vuln.get("description", "No description available.")
            score = cvss_score(vuln)

            # Register rule once
            if cve not in rules:
                rules[cve] = {
                    "id": cve,
                    "name": cve,
                    "shortDescription": {"text": cve},
                    "fullDescription": {"text": description[:1000]},
                    "defaultConfiguration": {"level": level},
                    "properties": {
                        "tags": ["security", "dependency"],
                        "precision": "medium",
                        "problem.severity": severity,
                        "security-severity": str(score),
                    },
                    "helpUri": f"https://nvd.nist.gov/vuln/detail/{cve}",
                }

            results.append({
                "ruleId": cve,
                "level": level,
                "message": {
                    "text": (
                        f"{cve} ({severity}, CVSS {score}) in {Path(file_path).name}. "
                        f"{description[:500]}"
                    )
                },
                "locations": [{
                    "physicalLocation": {
                        "artifactLocation": {"uri": file_path, "uriBaseId": "%SRCROOT%"},
                        "region": {"startLine": 1},
                    }
                }],
            })

    sarif = {
        "version": "2.1.0",
        "$schema": "https://json.schemastore.org/sarif-2.1.0.json",
        "runs": [{
            "tool": {
                "driver": {
                    "name": "OWASP Dependency-Check",
                    "version": report.get("reportSchema", "unknown"),
                    "informationUri": "https://jeremylong.github.io/DependencyCheck/",
                    "rules": list(rules.values()),
                }
            },
            "results": results,
        }],
    }

    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(sarif, f, indent=2)

    print(f"Converted {len(results)} findings ({len(rules)} unique CVEs) → {output_path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--input",  required=True, help="Path to dependency-check-report.json")
    parser.add_argument("--output", required=True, help="Path for output SARIF file")
    args = parser.parse_args()

    if not Path(args.input).exists():
        print(f"Input not found: {args.input}", file=sys.stderr)
        sys.exit(1)

    convert(args.input, args.output)
