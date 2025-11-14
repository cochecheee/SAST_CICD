import json
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--input', required=True)
parser.add_argument('--output', required=True)
args = parser.parse_args()

with open(args.input, 'r') as f:
    sarif = json.load(f)

issues = []
for run in sarif.get('runs', []):
    for result in run.get('results', []):
        issue = {
            'engineId': 'CodeQL',
            'ruleId': result['ruleId'],
            'primaryLocation': {
                'message': {'text': result['message']['text']},
                'filePath': result['locations'][0]['physicalLocation']['artifactLocation']['uri'],
                'textRange': {
                    'startLine': result['locations'][0]['physicalLocation']['region']['startLine']
                }
            }
        }
        issues.append(issue)

with open(args.output, 'w') as f:
    json.dump({'issues': issues}, f)