#!/usr/bin/env python3
"""
Notify — Pipeline results to Web Dashboard + Email
====================================================
Reads run-metadata.json and gate1-result.json, then dispatches:
  1. HTTP POST to DASHBOARD_WEBHOOK_URL  (centralised dashboard REST API)
  2. HTML + plain-text email via SMTP

Required environment variables
───────────────────────────────
  DASHBOARD_WEBHOOK_URL   Full URL of the dashboard /webhook/pipeline-complete endpoint
  DASHBOARD_TOKEN         Bearer token (omit if the endpoint is open)

  SMTP_HOST               SMTP server hostname  (e.g. smtp.gmail.com)
  SMTP_PORT               SMTP port             (default: 587, STARTTLS)
  SMTP_USER               Login username
  SMTP_PASS               Login password
  EMAIL_FROM              Sender address        (defaults to SMTP_USER)
  EMAIL_TO                Comma-separated recipient list
  EMAIL_CC                Optional CC list

Usage (called from ci.yml):
  python scripts/notify.py \
      --metadata   run-metadata.json \
      --gate1      gate1-result.json \
      --status     gate_failed       \
      [--no-email] [--no-dashboard]
"""

import argparse
import json
import os
import smtplib
import sys
from datetime import datetime, timezone
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from pathlib import Path
from typing import Any, Dict
import urllib.request
import urllib.error


# ── Helpers ─────────────────────────────────────────────────────────────────

def _load_json(path: str) -> Dict[str, Any]:
    p = Path(path)
    if p.exists():
        with open(p, encoding="utf-8") as f:
            return json.load(f)
    return {}


def _now_iso() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


_STATUS_LABEL = {
    "passed":      "PASSED ✅",
    "gate_failed": "GATE FAILED 🔴",
    "failed":      "FAILED ❌",
}
_STATUS_COLOR = {
    "passed":      "#28a745",
    "gate_failed": "#dc3545",
    "failed":      "#dc3545",
}


# ── Subject line ─────────────────────────────────────────────────────────────

def _subject(meta: dict, status: str) -> str:
    repo   = meta.get("repository", "unknown")
    run_no = meta.get("run_number", "?")
    branch = meta.get("ref", "refs/heads/?").replace("refs/heads/", "")
    label  = _STATUS_LABEL.get(status, status.upper())
    return f"[{repo}] Security Scan #{run_no} on {branch}: {label}"


# ── HTML email body ──────────────────────────────────────────────────────────

def _html(meta: dict, gate1: dict, status: str) -> str:
    repo    = meta.get("repository", "unknown")
    run_id  = meta.get("run_id", "")
    run_no  = meta.get("run_number", "?")
    branch  = meta.get("ref", "?").replace("refs/heads/", "")
    sha     = meta.get("sha", "")[:8]
    actor   = meta.get("actor", "unknown")
    ts      = meta.get("timestamp", _now_iso())

    color   = _STATUS_COLOR.get(status, "#fd7e14")
    label   = _STATUS_LABEL.get(status, status.upper())

    by_sev     = gate1.get("by_severity", {})
    gate1_ok   = gate1.get("passed", True)
    gate1_msg  = gate1.get("message", "N/A")
    gate1_icon = "✅" if gate1_ok else "🔴"
    gate1_color = "#28a745" if gate1_ok else "#dc3545"

    def _count_cell(sev: str, bg: str) -> str:
        n = by_sev.get(sev, 0)
        return (
            f'<td style="padding:6px 14px;background:{bg};font-weight:bold">{sev}</td>'
            f'<td style="padding:6px 14px;border:1px solid #dee2e6;text-align:center">{n}</td>'
        )

    # Critical finding rows (up to 10)
    crit_rows = ""
    for f in gate1.get("critical_findings", [])[:10]:
        crit_rows += (
            f'<tr>'
            f'<td style="padding:4px 8px;border:1px solid #dee2e6">{f.get("tool","")}</td>'
            f'<td style="padding:4px 8px;border:1px solid #dee2e6;font-size:12px">{f.get("rule_id","")}</td>'
            f'<td style="padding:4px 8px;border:1px solid #dee2e6;font-size:12px">{f.get("location","")}</td>'
            f'<td style="padding:4px 8px;border:1px solid #dee2e6;font-size:11px">{f.get("message","")[:140]}</td>'
            f'</tr>'
        )
    crit_table = ""
    if crit_rows:
        crit_table = f"""
        <h3 style="color:#dc3545;margin-top:20px">Critical Findings</h3>
        <table style="border-collapse:collapse;width:100%;font-size:13px">
          <tr style="background:#f8d7da;font-weight:bold">
            <th style="padding:4px 8px;border:1px solid #dee2e6">Tool</th>
            <th style="padding:4px 8px;border:1px solid #dee2e6">Rule</th>
            <th style="padding:4px 8px;border:1px solid #dee2e6">Location</th>
            <th style="padding:4px 8px;border:1px solid #dee2e6">Message</th>
          </tr>
          {crit_rows}
        </table>"""

    gh_url = f"https://github.com/{repo}/actions/runs/{run_id}"

    return f"""\
<!DOCTYPE html>
<html lang="en">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
<body style="font-family:Arial,Helvetica,sans-serif;max-width:820px;margin:0 auto;padding:20px;color:#212529">

  <!-- Header -->
  <div style="background:{color};color:#fff;padding:16px 24px;border-radius:6px 6px 0 0">
    <h2 style="margin:0;font-size:20px">Security Scan: {label}</h2>
    <p style="margin:6px 0 0;font-size:14px;opacity:.9">
      <strong>{repo}</strong> &nbsp;·&nbsp; branch: <strong>{branch}</strong>
    </p>
  </div>

  <!-- Body -->
  <div style="border:1px solid #dee2e6;border-top:none;padding:24px;border-radius:0 0 6px 6px">

    <!-- Run metadata -->
    <table style="font-size:14px;border-collapse:collapse;margin-bottom:20px">
      <tr><td style="padding:3px 0;color:#6c757d;width:130px">Run</td>
          <td><strong>#{run_no}</strong></td></tr>
      <tr><td style="padding:3px 0;color:#6c757d">Commit</td>
          <td><code style="background:#f8f9fa;padding:1px 4px;border-radius:3px">{sha}</code></td></tr>
      <tr><td style="padding:3px 0;color:#6c757d">Triggered by</td>
          <td>{actor}</td></tr>
      <tr><td style="padding:3px 0;color:#6c757d">Timestamp</td>
          <td style="font-size:13px">{ts}</td></tr>
    </table>

    <!-- Gate 1 -->
    <h3 style="margin-bottom:6px">
      {gate1_icon} Gate 1 — SARIF Threshold
    </h3>
    <p style="color:{gate1_color};font-size:14px;margin:0 0 12px">{gate1_msg}</p>
    <table style="border-collapse:collapse;font-size:14px">
      <tr>
        {_count_cell("CRITICAL", "#f8d7da")}
        {_count_cell("HIGH",     "#fff3cd")}
        {_count_cell("MEDIUM",   "#fff9e6")}
        {_count_cell("LOW",      "#d4edda")}
        {_count_cell("INFO",     "#e2e3e5")}
      </tr>
    </table>

    {crit_table}

    <!-- Gate 2 note -->
    <h3 style="margin-top:24px;margin-bottom:4px">Gate 2 — SonarCloud Quality Gate</h3>
    <p style="font-size:14px;color:#6c757d;margin:0">
      Result available on the SonarCloud dashboard and in the GitHub Actions run log.
    </p>

    <!-- Gate 3 note -->
    <h3 style="margin-top:20px;margin-bottom:4px">Gate 3 — Branch Protection</h3>
    <p style="font-size:14px;color:#6c757d;margin:0">
      GitHub branch rule enforces <code>CI / Security Gate</code> status check.
      The PR commit status has been updated automatically.
    </p>

    <!-- CTA -->
    <div style="margin-top:28px">
      <a href="{gh_url}"
         style="background:#0d6efd;color:#fff;padding:10px 22px;border-radius:4px;
                text-decoration:none;font-size:14px;font-weight:bold">
        View Pipeline Run →
      </a>
    </div>

  </div>

  <p style="font-size:11px;color:#adb5bd;margin-top:14px">
    CI Security Scanner &nbsp;·&nbsp; {repo} &nbsp;·&nbsp; {ts}
  </p>
</body>
</html>"""


# ── Plain-text email body ────────────────────────────────────────────────────

def _plain(meta: dict, gate1: dict, status: str) -> str:
    repo    = meta.get("repository", "unknown")
    run_no  = meta.get("run_number", "?")
    run_id  = meta.get("run_id", "")
    branch  = meta.get("ref", "?").replace("refs/heads/", "")
    sha     = meta.get("sha", "")[:8]
    actor   = meta.get("actor", "unknown")
    ts      = meta.get("timestamp", _now_iso())
    by_sev  = gate1.get("by_severity", {})
    label   = _STATUS_LABEL.get(status, status.upper())

    lines = [
        f"Security Scan: {label}",
        "=" * 50,
        f"Repository : {repo}",
        f"Branch     : {branch}",
        f"Run        : #{run_no}",
        f"Commit     : {sha}",
        f"Actor      : {actor}",
        f"Timestamp  : {ts}",
        "",
        "Gate 1 — SARIF Threshold",
        "-" * 30,
        f"Result  : {gate1.get('message','N/A')}",
        f"CRITICAL: {by_sev.get('CRITICAL', 0)}",
        f"HIGH    : {by_sev.get('HIGH', 0)}",
        f"MEDIUM  : {by_sev.get('MEDIUM', 0)}",
        f"LOW     : {by_sev.get('LOW', 0)}",
        "",
    ]
    for f in gate1.get("critical_findings", [])[:10]:
        lines.append(f"  [{f.get('tool','')}] {f.get('rule_id','')}  @  {f.get('location','')}")
        lines.append(f"    {f.get('message','')[:140]}")
    lines += [
        "",
        "Gate 2 — SonarCloud Quality Gate: see GitHub Actions log",
        "Gate 3 — Branch Protection: PR commit status updated",
        "",
        f"View run: https://github.com/{repo}/actions/runs/{run_id}",
    ]
    return "\n".join(lines)


# ── Channel: Dashboard ───────────────────────────────────────────────────────

def _send_dashboard(payload: dict) -> bool:
    url   = os.environ.get("DASHBOARD_WEBHOOK_URL", "").strip()
    token = os.environ.get("DASHBOARD_TOKEN", "").strip()

    if not url:
        print("  DASHBOARD_WEBHOOK_URL not set — skipping dashboard notification.")
        return True

    data = json.dumps(payload).encode("utf-8")
    headers = {"Content-Type": "application/json", "User-Agent": "CI-Notifier/1.0"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=data, method="POST", headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            print(f"  Dashboard: HTTP {resp.status} ✓")
            return True
    except urllib.error.HTTPError as exc:
        body = exc.read(256).decode("utf-8", errors="replace")
        print(f"  Dashboard: HTTP {exc.code} {exc.reason} — {body}", file=sys.stderr)
        return False
    except Exception as exc:
        print(f"  Dashboard error: {exc}", file=sys.stderr)
        return False


# ── Channel: Email ───────────────────────────────────────────────────────────

def _send_email(subject: str, html_body: str, plain_body: str) -> bool:
    smtp_host = os.environ.get("SMTP_HOST", "").strip()
    smtp_port = int(os.environ.get("SMTP_PORT", "587"))
    smtp_user = os.environ.get("SMTP_USER", "").strip()
    smtp_pass = os.environ.get("SMTP_PASS", "").strip()
    from_addr = os.environ.get("EMAIL_FROM", smtp_user).strip() or smtp_user
    to_raw    = os.environ.get("EMAIL_TO",  "").strip()
    cc_raw    = os.environ.get("EMAIL_CC",  "").strip()

    if not smtp_host:
        print("  SMTP_HOST not set — skipping email notification.")
        return True
    if not to_raw:
        print("  EMAIL_TO not set — skipping email notification.")
        return True

    to_list = [e.strip() for e in to_raw.split(",")  if e.strip()]
    cc_list = [e.strip() for e in cc_raw.split(",")  if e.strip()]

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"]    = from_addr
    msg["To"]      = ", ".join(to_list)
    if cc_list:
        msg["Cc"] = ", ".join(cc_list)

    msg.attach(MIMEText(plain_body, "plain", "utf-8"))
    msg.attach(MIMEText(html_body,  "html",  "utf-8"))

    all_rcpt = to_list + cc_list
    try:
        with smtplib.SMTP(smtp_host, smtp_port, timeout=30) as srv:
            srv.ehlo()
            srv.starttls()
            srv.ehlo()
            if smtp_user and smtp_pass:
                srv.login(smtp_user, smtp_pass)
            srv.sendmail(from_addr, all_rcpt, msg.as_string())
        print(f"  Email sent → {', '.join(all_rcpt)} ✓")
        return True
    except smtplib.SMTPAuthenticationError as exc:
        print(f"  Email auth failed: {exc}", file=sys.stderr)
        return False
    except Exception as exc:
        print(f"  Email error: {exc}", file=sys.stderr)
        return False


# ── Entry point ──────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Send pipeline notifications to dashboard and/or email."
    )
    parser.add_argument("--metadata",     default="run-metadata.json",
                        help="Path to run-metadata.json (written by the notify job)")
    parser.add_argument("--gate1",        default="gate1-result.json",
                        help="Path to gate1-result.json (written by gate1_sarif_check.py)")
    parser.add_argument("--status",       required=True,
                        choices=["passed", "gate_failed", "failed"],
                        help="Overall pipeline status")
    parser.add_argument("--no-email",     action="store_true",
                        help="Skip email notification")
    parser.add_argument("--no-dashboard", action="store_true",
                        help="Skip dashboard webhook")
    args = parser.parse_args()

    meta  = _load_json(args.metadata)
    gate1 = _load_json(args.gate1)

    subject    = _subject(meta, args.status)
    html_body  = _html(meta, gate1, args.status)
    plain_body = _plain(meta, gate1, args.status)

    dashboard_payload = {
        "event":     "pipeline_complete",
        "status":    args.status,
        "metadata":  meta,
        "gate1":     gate1,
        "timestamp": _now_iso(),
    }

    print(f"\n── Sending notifications (status: {args.status}) ──")
    ok = True

    if not args.no_dashboard:
        ok = _send_dashboard(dashboard_payload) and ok

    if not args.no_email:
        ok = _send_email(subject, html_body, plain_body) and ok

    print(f"── Done (all_ok={ok}) ──\n")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
