#!/usr/bin/env python3
"""
Convert CodeQL SARIF format to SonarQube External Issues format
"""
import json
import sys
import argparse
from pathlib import Path


def convert_severity(sarif_level):
    """Convert SARIF level to Sonar severity"""
    mapping = {
        "error": "CRITICAL",
        "warning": "MAJOR",
        "note": "MINOR",
        "none": "INFO"
    }
    return mapping.get(sarif_level.lower(), "MAJOR")


def convert_type(sarif_tags):
    """Convert SARIF tags to Sonar issue type"""
    if not sarif_tags:
        return "CODE_SMELL"

    tags_lower = [tag.lower() for tag in sarif_tags]

    if any(tag in tags_lower for tag in ["security", "external/cwe"]):
        return "VULNERABILITY"
    elif any(tag in tags_lower for tag in ["correctness", "bug"]):
        return "BUG"
    else:
        return "CODE_SMELL"


def extract_rule_info(rule, run_tool):
    """Extract rule information from SARIF"""
    rule_id = rule.get("id", "unknown")

    # Get rule name
    name = rule.get("shortDescription", {}).get("text", rule_id)

    # Get description
    description = rule.get("fullDescription", {}).get("text", name)

    # Get tags
    properties = rule.get("properties", {})
    tags = properties.get("tags", [])

    return {
        "id": rule_id,
        "name": name,
        "description": description,
        "tags": tags
    }


def convert_sarif_to_sonar(sarif_file, output_file):
    """Main conversion function"""
    try:
        with open(sarif_file, 'r', encoding='utf-8') as f:
            sarif_data = json.load(f)
    except FileNotFoundError:
        print(f"❌ Error: Input file '{sarif_file}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"❌ Error: Invalid JSON in '{sarif_file}': {e}", file=sys.stderr)
        sys.exit(1)

    sonar_issues = {"issues": []}

    # Process each run in SARIF
    for run in sarif_data.get("runs", []):
        tool = run.get("tool", {}).get("driver", {})
        tool_name = tool.get("name", "CodeQL")

        # Build rule lookup
        rules = {}
        for rule in tool.get("rules", []):
            rule_info = extract_rule_info(rule, tool_name)
            rules[rule_info["id"]] = rule_info

        # Process results
        for result in run.get("results", []):
            rule_id = result.get("ruleId", "unknown")
            rule_info = rules.get(rule_id, {
                "id": rule_id,
                "name": rule_id,
                "description": "No description available",
                "tags": []
            })

            # Get primary location
            locations = result.get("locations", [])
            if not locations:
                continue

            primary_location = locations[0].get("physicalLocation", {})
            artifact_location = primary_location.get("artifactLocation", {})
            region = primary_location.get("region", {})

            file_path = artifact_location.get("uri", "unknown")
            start_line = region.get("startLine", 1)
            end_line = region.get("endLine", start_line)
            start_column = region.get("startColumn", 1)
            end_column = region.get("endColumn", start_column)

            # Get message
            message = result.get("message", {}).get("text", rule_info["description"])

            # Get severity
            level = result.get("level", "warning")
            severity = convert_severity(level)

            # Get type
            issue_type = convert_type(rule_info["tags"])

            # Build Sonar issue
            sonar_issue = {
                "engineId": tool_name,
                "ruleId": rule_id,
                "severity": severity,
                "type": issue_type,
                "primaryLocation": {
                    "message": message,
                    "filePath": file_path,
                    "textRange": {
                        "startLine": start_line,
                        "endLine": end_line,
                        "startColumn": start_column - 1,  # Sonar uses 0-based columns
                        "endColumn": end_column - 1
                    }
                }
            }

            # Add secondary locations if available
            if len(locations) > 1:
                secondary_locations = []
                for loc in locations[1:]:
                    phys_loc = loc.get("physicalLocation", {})
                    sec_artifact = phys_loc.get("artifactLocation", {})
                    sec_region = phys_loc.get("region", {})

                    secondary_locations.append({
                        "message": loc.get("message", {}).get("text", "Related location"),
                        "filePath": sec_artifact.get("uri", file_path),
                        "textRange": {
                            "startLine": sec_region.get("startLine", 1),
                            "endLine": sec_region.get("endLine", 1)
                        }
                    })

                if secondary_locations:
                    sonar_issue["secondaryLocations"] = secondary_locations

            sonar_issues["issues"].append(sonar_issue)

    # Write output
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(sonar_issues, f, indent=2)

        print(f"✅ Successfully converted {len(sonar_issues['issues'])} issues")
        print(f"✅ Output written to: {output_file}")
        return 0
    except Exception as e:
        print(f"❌ Error writing output file: {e}", file=sys.stderr)
        return 1


def main():
    parser = argparse.ArgumentParser(
        description="Convert CodeQL SARIF format to SonarQube External Issues format"
    )
    parser.add_argument(
        "--input",
        "-i",
        required=True,
        help="Input SARIF file from CodeQL"
    )
    parser.add_argument(
        "--output",
        "-o",
        required=True,
        help="Output JSON file for SonarQube"
    )

    args = parser.parse_args()

    return convert_sarif_to_sonar(args.input, args.output)


if __name__ == "__main__":
    sys.exit(main())