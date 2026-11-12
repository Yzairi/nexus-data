#!/usr/bin/env python3
"""Crée un petit échantillon cohérent : 10 logs maximum et leurs sessions de télémétrie."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


SESSION_RE = re.compile(r"\bsession(?:_id)?=(\S+)")
PLAYER_RE = re.compile(r"\bplayer(?:_id)?=(P\d{8})")


def fields(line: str) -> tuple[str | None, str | None]:
    text = line.strip()
    if text.startswith("{"):
        row = json.loads(text)
        return (
            row.get("session_id", row.get("session")),
            row.get("player_id", row.get("player")),
        )
    session = SESSION_RE.search(text)
    player = PLAYER_RE.search(text)
    return (
        session.group(1) if session else None,
        player.group(1) if player else None,
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=Path("../data/source"))
    parser.add_argument("--date", default="2026-08-21")
    parser.add_argument("--max-logs", type=int, default=10)
    parser.add_argument("--max-telemetry", type=int, default=200)
    parser.add_argument("--output", type=Path, default=Path("data/sample"))
    args = parser.parse_args()

    if args.max_logs < 1 or args.max_logs > 10:
        parser.error("--max-logs doit être compris entre 1 et 10")

    log_files = sorted((args.source / "logs" / f"dt={args.date}").glob("*.log"))
    telemetry_files = sorted((args.source / "telemetry" / f"dt={args.date}").glob("*.jsonl"))
    if not log_files or not telemetry_files:
        parser.error("Partition source introuvable")

    log_lines = []
    sessions = set()
    players = set()
    for path in log_files:
        for line in path.open(encoding="utf-8", errors="replace"):
            if not line.strip():
                continue
            log_lines.append(line.rstrip("\n"))
            session, player = fields(line)
            if session:
                sessions.add(session)
            if player:
                players.add(player)
            if len(log_lines) >= args.max_logs:
                break
        if len(log_lines) >= args.max_logs:
            break

    telemetry_lines = []
    for path in telemetry_files:
        for line in path.open(encoding="utf-8", errors="replace"):
            try:
                row = json.loads(line)
            except json.JSONDecodeError:
                continue
            session = row.get("session_id", row.get("session"))
            if session not in sessions:
                continue
            telemetry_lines.append(line.rstrip("\n"))
            player = row.get("player_id", row.get("pid"))
            if player:
                players.add(player)
            if len(telemetry_lines) >= args.max_telemetry:
                break
        if len(telemetry_lines) >= args.max_telemetry:
            break

    player_lines = []
    for line in (args.source / "mongo" / "players.jsonl").open(encoding="utf-8", errors="replace"):
        try:
            row = json.loads(line)
        except json.JSONDecodeError:
            continue
        if row.get("_id") in players:
            player_lines.append(line.rstrip("\n"))

    for name in ("logs", "telemetry", "players"):
        (args.output / name).mkdir(parents=True, exist_ok=True)
    (args.output / "logs" / "sample.log").write_text("\n".join(log_lines) + "\n", encoding="utf-8")
    (args.output / "telemetry" / "sample.jsonl").write_text(
        "\n".join(telemetry_lines) + "\n", encoding="utf-8"
    )
    (args.output / "players" / "sample.jsonl").write_text(
        "\n".join(player_lines) + "\n", encoding="utf-8"
    )
    print(
        f"Échantillon créé : {len(log_lines)} logs, {len(telemetry_lines)} événements, "
        f"{len(player_lines)} joueurs, {len(sessions)} sessions."
    )


if __name__ == "__main__":
    main()
