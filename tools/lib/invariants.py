"""
Invariants and Safety Rules for CliminTV Debloat Subsystem.
Strictly prevents disabling critical system packages, Wi-Fi dropping, and bootloops.
"""

import re
from typing import List, Tuple

# Core Android & OEM System Packages that must NEVER be disabled under any circumstance.
# Disabling these causes immediate system_server crash, bootloops, or broken zygote initialization.
CORE_RE = re.compile(
    r"^(android|"
    r"com\.android\.systemui|"
    r"com\.android\.shell|"
    r"com\.android\.phone|"
    r"com\.android\.settings|"
    r"com\.android\.tv\.settings|"
    r"com\.(google\.)?android\.(permissioncontroller|packageinstaller|ext\.services|ext\.shared|modulemetadata)|"
    r"com\.google\.android\.overlay\.modules.*|"
    r"com\.android\.providers\..*)$"
)

# Providers that are explicitly safe to disable if the user does not use sync/calling
CORE_OK = re.compile(r"^com\.android\.providers\.(calendar|contacts|userdictionary|blockednumber)$")

# Setupwraith Wi-Fi drop trap package
SETUPWRAITH_PKG = "com.google.android.tungsten.setupwraith"

# Known OEM Stock Launchers that require the Home Guard verification before disabling
STOCK_LAUNCHERS = {
    "com.google.android.tvlauncher",
    "com.google.android.apps.tv.launcherx",
    "com.mitv.tvhome",
    "com.tcl.tvhome",
    "com.sony.dtv.home",
    "com.amazon.tv.launcher",
    "com.amlogic.tvlauncher",
    "com.realtek.tvlauncher",
}

# The replacement launcher package
OUR_LAUNCHER_PKG = "io.climintv.home"


def is_core_package(pkg: str) -> bool:
    """Returns True if package is protected by the CORE safety invariant."""
    pkg = pkg.strip()
    if not pkg:
        return False
    if CORE_OK.match(pkg):
        return False
    return bool(CORE_RE.match(pkg))


def validate_package_list(packages: List[str]) -> Tuple[List[str], List[str]]:
    """
    Separates input packages into safe to disable and forbidden (core system packages).
    Returns (safe_packages, blocked_packages).
    """
    safe = []
    blocked = []
    for pkg in packages:
        p = pkg.strip()
        if not p or p.startswith("#"):
            continue
        if is_core_package(p):
            blocked.append(p)
        else:
            safe.append(p)
    return safe, blocked


def check_setupwraith_wifi_ownership(dumpsys_wifi_output: str) -> bool:
    """
    Inspects `dumpsys wifi` output to check if Setupwraith currently owns the active Wi-Fi connection.
    If 'cname=com.google.android.tungsten.setupwraith' or similar matches the active network,
    disabling setupwraith will wipe Wi-Fi credentials and sever ADB over TCP.
    Returns True if Setupwraith owns the connection (UNSAFE to disable).
    """
    lines = dumpsys_wifi_output.splitlines()
    for line in lines:
        if "cname=" in line and SETUPWRAITH_PKG in line:
            return True
        if "creatorUid=" in line and SETUPWRAITH_PKG in line:
            return True
    return False
