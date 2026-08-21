#!/usr/bin/env bash
set -euo pipefail

fail() { printf 'linux runtime failed: %s\n' "$*" >&2; exit 125; }
has() { command -v "$1" >/dev/null 2>&1; }
require_linux() { [[ "$(uname -s)" == Linux ]] || fail "Linux is required"; }
require_cgroup() {
  [[ -r /sys/fs/cgroup/cgroup.controllers ]] || fail "cgroups v2 are required"
  has systemd-run || fail "systemd-run is required"
  timeout 5 systemctl --user show-environment >/dev/null 2>&1 || fail "a delegated systemd user manager is required"
}

doctor() {
  require_linux
  printf 'kernel=%s\n' "$(uname -r)"
  printf 'root.filesystem=%s\n' "$(findmnt -n -o FSTYPE / 2>/dev/null || printf unknown)"
  printf 'cgroup.v2=%s\n' "$([[ -r /sys/fs/cgroup/cgroup.controllers ]] && printf yes || printf no)"
  printf 'systemd.user=%s\n' "$(timeout 5 systemctl --user show-environment >/dev/null 2>&1 && printf yes || printf no)"
  printf 'bubblewrap=%s\n' "$(has bwrap && printf yes || printf no)"
  printf 'network.namespace=%s\n' "$(has bwrap && printf yes || printf no)"
}

self_test() {
  require_linux; require_cgroup
  local work; work="$(mktemp -d)"; trap "rm -rf -- '$work'" EXIT
  "$0" run --mode cgroup --memory 100663296 --cpu-percent 100 --processes 8 \
    --timeout-seconds 10 --cwd "$PWD" --log "$work/console.log" --metrics "$work/metrics.properties" \
    -- /bin/sh -c 'printf linux-runtime-self-test'
  grep -q linux-runtime-self-test "$work/console.log" || fail "output capture drift"
  grep -q '^backend=linux-cgroup$' "$work/metrics.properties" || fail "metrics drift"
  if has bwrap; then
    "$0" run --mode sandbox --memory 100663296 --cpu-percent 100 --processes 8 \
      --timeout-seconds 10 --cwd "$PWD" --log "$work/sealed.log" --metrics "$work/sealed.properties" \
      -- /bin/sh -c 'test "$(cat /proc/1/comm)" = bwrap && grep -q "lo:" /proc/net/dev && printf linux-sandbox-self-test'
    grep -q linux-sandbox-self-test "$work/sealed.log" || fail "sandbox probe drift"
  fi
  printf 'linux runtime self-test passed\n'
}

run() {
  local mode= memory= cpu= processes= timeout= cwd= log= metrics=
  while [[ $# -gt 0 && "$1" != -- ]]; do
    [[ $# -ge 2 ]] || fail "missing value for $1"
    case "$1" in
      --mode) mode="$2";; --memory) memory="$2";; --cpu-percent) cpu="$2";;
      --processes) processes="$2";; --timeout-seconds) timeout="$2";; --cwd) cwd="$2";;
      --log) log="$2";; --metrics) metrics="$2";; *) fail "unknown option $1";;
    esac; shift 2
  done
  [[ $# -ge 2 && "$1" == -- ]] || fail "missing -- command separator"; shift
  [[ "$mode" == cgroup || "$mode" == sandbox ]] || fail "mode must be cgroup or sandbox"
  [[ "$memory" =~ ^[0-9]+$ && "$cpu" =~ ^[0-9]+$ && "$processes" =~ ^[0-9]+$ && "$timeout" =~ ^[0-9]+$ ]] || fail "limits must be integers"
  [[ -d "$cwd" && -n "$log" && -n "$metrics" ]] || fail "invalid paths"
  require_linux; require_cgroup; mkdir -p -- "$(dirname "$log")" "$(dirname "$metrics")" "$cwd/.worldline"
  local -a command=("$@")
  if [[ "$mode" == sandbox ]]; then
    has bwrap || fail "bubblewrap is required for sealed isolation"
    command=(bwrap --die-with-parent --new-session --unshare-user --unshare-pid --unshare-ipc --unshare-uts
      --unshare-net --cap-add CAP_NET_ADMIN --ro-bind / / --dev /dev --proc /proc
      --bind "$cwd/.worldline" "$cwd/.worldline" --chdir "$cwd"
      /bin/sh -c 'exec "$@"' worldline "${command[@]}")
  fi
  local started elapsed status=0; started="$(cut -d' ' -f1 /proc/uptime)"
  systemd-run --user --wait --collect --pipe --quiet --working-directory="$cwd" \
    -p "MemoryMax=$memory" -p "CPUQuota=$cpu%" -p "TasksMax=$processes" \
    -p KillMode=control-group -p TimeoutStopSec=5s \
    /usr/bin/timeout --signal=KILL "${timeout}s" "${command[@]}" >"$log" 2>&1 || status=$?
  elapsed="$(awk -v start="$started" '{printf "%.0f", ($1-start)*1000}' /proc/uptime)"
  {
    printf 'format=1\nbackend=linux-%s\nexit.code=%s\n' "$mode" "$status"
    printf 'elapsed.millis=%s\nmemory.limit.bytes=%s\ncpu.limit.percent=%s\nprocess.limit=%s\n' \
      "$elapsed" "$memory" "$cpu" "$processes"
  } >"$metrics"
  return "$status"
}

case "${1:-}" in
  doctor) [[ $# == 1 ]] || fail "doctor takes no arguments"; doctor;;
  --self-test) [[ $# == 1 ]] || fail "self-test takes no arguments"; self_test;;
  run) shift; run "$@";;
  *) fail "usage: linux-runtime.sh doctor|--self-test|run [options] -- COMMAND";;
esac
