import json
import sys

def sarif_to_sonarqube(sarif_file_path, output_file_path):
    with open(sarif_file_path, 'r', encoding='utf-8') as f:
        sarif_data = json.load(f)

    sonar_issues = []

    for run in sarif_data.get("runs", []):
        for result in run.get("results", []):
            rule_id = result.get("ruleId")
            level = result.get("level", "warning")  # note, warning, error
            message = result.get("message", {}).get("text", "No message")
            locations = result.get("locations", [])

            for loc in locations:
                artifact_location = loc.get("physicalLocation", {}).get("artifactLocation")
                region = loc.get("physicalLocation", {}).get("region")

                if not artifact_location:
                    continue

                file_path = artifact_location.get("uri")
                if file_path.startswith("file://"):
                    file_path = file_path[7:]  # remove file:// prefix

                start_line = region.get("startLine", 1)
                end_line = region.get("endLine", start_line)
                start_col = region.get("startColumn", 0)
                end_col = region.get("endColumn", start_col)

                # Map SARIF level to SonarQube severity
                severity_map = {
                    "note": "INFO",
                    "warning": "MINOR",
                    "error": "CRITICAL"
                }
                severity = severity_map.get(level, "MAJOR")

                # Determine issue type
                issue_type = "BUG"
                if "security" in rule_id.lower() or "cwe" in rule_id.lower():
                    issue_type = "VULNERABILITY"

                issue = {
                    "engineId": "codeql",
                    "ruleId": rule_id,
                    "severity": severity,
                    "type": issue_type,
                    "primaryLocation": {
                        "message": message,
                        "filePath": file_path,
                        "textRange": {
                            "startLine": start_line,
                            "endLine": end_line,
                            "startColumn": start_col,
                            "endColumn": end_col
                        }
                    }
                }
                sonar_issues.append(issue)

    with open(output_file_path, 'w', encoding='utf-8') as out:
        json.dump(sonar_issues, out, indent=2)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python sarif_to_sonar.py <input.sarif> <output.json>")
        sys.exit(1)

    sarif_input = sys.argv[1]
    json_output = sys.argv[2]

    sarif_to_sonarqube(sarif_input, json_output)
    print(f"Converted {sarif_input} to {json_output}")