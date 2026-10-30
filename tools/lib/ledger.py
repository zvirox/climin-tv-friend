"""
State Snapshot & Recovery Ledger for Your TV.
Maintains reversible transaction history of package state modifications.
"""

import os
import json
import time
from typing import List, Dict, Any, Optional

DEFAULT_STATE_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "state")


def ensure_state_dir(state_dir: Optional[str] = None) -> str:
    path = state_dir or DEFAULT_STATE_DIR
    os.makedirs(path, exist_ok=True)
    return path


def create_ledger(
    device_id: str,
    action: str,
    packages: List[str],
    metadata: Optional[Dict[str, Any]] = None,
    state_dir: Optional[str] = None
) -> str:
    """
    Records an atomic snapshot transaction ledger.
    Returns path to created ledger file.
    """
    directory = ensure_state_dir(state_dir)
    timestamp = int(time.time())
    timestr = time.strftime("%Y%m%d_%H%M%S", time.localtime(timestamp))
    filename = f"{action}_{timestr}_{device_id.replace(':', '_')}.json"
    filepath = os.path.join(directory, filename)

    payload = {
        "timestamp": timestamp,
        "date_human": time.ctime(timestamp),
        "device_id": device_id,
        "action": action,
        "packages": packages,
        "count": len(packages),
        "metadata": metadata or {}
    }

    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(payload, f, indent=2)

    # Also update 'latest.json' symlink or copy
    latest_path = os.path.join(directory, "latest.json")
    try:
        with open(latest_path, "w", encoding="utf-8") as f:
            json.dump(payload, f, indent=2)
    except Exception:
        pass

    return filepath


def load_ledger(filepath: str) -> Dict[str, Any]:
    """Loads a ledger file."""
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


def list_ledgers(state_dir: Optional[str] = None) -> List[str]:
    """Returns list of all available transaction ledgers sorted chronologically (newest first)."""
    directory = ensure_state_dir(state_dir)
    files = [
        os.path.join(directory, f)
        for f in os.listdir(directory)
        if f.endswith(".json") and f != "latest.json"
    ]
    files.sort(key=os.path.getmtime, reverse=True)
    return files
