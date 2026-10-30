# Your TV - Android TV Architecture & Material 3 Expressive Launcher
# Build, Deployment, Debloating, and Security Hardening Automation

SHELL := /usr/bin/env bash

.PHONY: all help build install disable restore verify-closed clean

all: help

help:
	@echo "Your TV - Android TV Toolkit & Material 3 Expressive Launcher"
	@echo ""
	@echo "Usage commands:"
	@echo "  make build                     Build release APK for Your TV Home"
	@echo "  make install TV_HOST=<ip>       Build & install Your TV Home on target TV"
	@echo "  make disable TV_HOST=<ip> LIST=<path>  Run non-destructive debloat list"
	@echo "  make restore TV_HOST=<ip> LIST=<path>  Revert and restore disabled packages"
	@echo "  make check LIST=<path>         Verify package safety invariants against CORE_RE"
	@echo "  make verify-closed TV_HOST=<ip> Audit device network security (port 5555 closed)"
	@echo "  make clean                     Clean Gradle build artifacts"

build:
	cd launcher && ./gradlew assembleRelease --console=plain

install:
	@if [ -z "$(TV_HOST)" ]; then echo "Error: TV_HOST is required. Example: make install TV_HOST=192.168.1.50"; exit 1; fi
	cd launcher && TV_HOST=$(TV_HOST) ./build-and-install.sh

disable:
	@if [ -z "$(TV_HOST)" ]; then echo "Error: TV_HOST is required. Example: make disable TV_HOST=192.168.1.50 LIST=examples/xiaomi-mitv-aetp0-dofus/debloat.txt"; exit 1; fi
	@if [ -z "$(LIST)" ]; then echo "Error: LIST is required."; exit 1; fi
	TV_HOST=$(TV_HOST) ./tools/disable.sh $(LIST)

restore:
	@if [ -z "$(TV_HOST)" ]; then echo "Error: TV_HOST is required. Example: make restore TV_HOST=192.168.1.50 LIST=examples/xiaomi-mitv-aetp0-dofus/debloat.txt"; exit 1; fi
	@if [ -z "$(LIST)" ]; then echo "Error: LIST is required."; exit 1; fi
	TV_HOST=$(TV_HOST) ./tools/restore.sh $(LIST)

check:
	@if [ -z "$(LIST)" ]; then echo "Error: LIST is required. Example: make check LIST=examples/acer-ar55ar2851udpro/debloat.txt"; exit 1; fi
	python3 tools/check.py $(LIST)

verify-closed:
	@if [ -z "$(TV_HOST)" ]; then echo "Error: TV_HOST is required. Example: make verify-closed TV_HOST=192.168.1.50"; exit 1; fi
	python3 tools/adb-closed.py $(TV_HOST)

clean:
	cd launcher && ./gradlew clean --console=plain
