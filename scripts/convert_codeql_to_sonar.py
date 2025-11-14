import json
import argparse
import os

def convert_sarif_to_sonar(sarif_path, sonar_path):
    with open(sarif_path, 'r') as f:
        sarif = json.load(f)

    issues = []
    for run in sarif.get('runs', []):
        tool_name = run['tool']['driver']['name']
        for result in run.get('results', []):
            rule_id = result['ruleId']
            message = result['message']['text']
            level = result.get('level', 'warning')  # warning, error, note

            # Map severity
            severity = 'MINOR'
            if level == 'error':
                severity = 'CRITICAL'
            elif level == 'warning':
                severity = 'MAJOR'

            for location in result.get('locations', []):
                try:
                    file_path = location['physicalLocation']['artifactLocation']['uri']
                    start_line = location['physicalLocation']['region'].get('startLine', 1)
                    end_line = location['physicalLocation']['region'].get('endLine', start_line)
                    start_column = location['physicalLocation']['region'].get('startColumn', 1)
                    end_column = location['physicalLocation']['region'].get('endColumn', start_column)

                    issue = {
                        'engineId': tool_name,
                        'ruleId': rule_id,
                        'severity': severity,
                        'type': 'VULNERABILITY' if 'security' in rule_id.lower() else 'BUG',
                        'primaryLocation': {
                            'message': message,
                            'filePath': file_path,
                            'textRange': {
                                'startLine': start_line,
                                'endLine': end_line,
                                'startColumn': start_column - 1,  # Sonar uses 0-based columns
                                'endColumn': end_column - 1
                            }
                        }
                    }
                    issues.append(issue)
                except KeyError:
                    continue  # Skip invalid locations

    with open(sonar_path, 'w') as f:
        json.dump({'issues': issues}, f, indent=2)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Convert CodeQL SARIF to SonarQube external issues format')
    parser.add_argument('--input', required=True, help='Input SARIF file')
    parser.add_argument('--output', required=True, help='Output Sonar JSON file')
    args = parser.parse_args()

    if not os.path.exists(args.input):
        print(f"Error: Input file {args.input} not found. Creating empty report.")
        with open(args.output, 'w') as f:
            json.dump({'issues': []}, f)
    else:
        convert_sarif_to_sonar(args.input, args.output)