import csv
import math
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(ROOT, "results", "results.csv")
PLOT_DIR = os.path.join(ROOT, "results", "plots")
ANALYSIS_PATH = os.path.join(ROOT, "results", "analysis.txt")

ALGOS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = {
    "Kruskal": "#0072B2",
    "Prim": "#D55E00",
    "Kosaraju": "#009E73",
    "Kahn": "#CC79A7",
    "DagShortestPath": "#E69F00",
}
MARKERS = {"Kruskal": "o", "Prim": "s", "Kosaraju": "^", "Kahn": "D", "DagShortestPath": "v"}
STYLE = {"sparse": "-", "dense": "--"}


def load():
    rows = []
    with open(CSV_PATH, newline="") as f:
        for r in csv.DictReader(f):
            rows.append({
                "algorithm": r["algorithm"],
                "density": r["graph"].split("_")[1],
                "n": int(r["n"]),
                "m": int(r["m"]),
                "time_ms": float(r["time_ms"]),
                "dsu_ops": int(r["dsu_ops"]),
                "edge_scans": int(r["edge_scans"]),
                "comparisons": int(r["comparisons"]),
                "max_depth": int(r["max_depth"]),
                "result": int(r["result"]),
            })
    return rows


def series(rows, algo, density):
    pts = [r for r in rows if r["algorithm"] == algo and r["density"] == density]
    return sorted(pts, key=lambda r: r["n"])


def style_axes(ax, title, xlabel, ylabel):
    ax.set_title(title, fontsize=13, loc="left", pad=12)
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.grid(True, which="major", color="#e3e3e3", linewidth=0.8)
    ax.set_axisbelow(True)
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color("#9a9a9a")
    ax.tick_params(colors="#444444")


def legend_both(ax, loc, outside=False):
    handles = []
    for a in ALGOS:
        handles.append(plt.Line2D([0], [0], color=COLORS[a], marker=MARKERS[a], linewidth=2, label=a))
    handles.append(plt.Line2D([0], [0], color="#555555", linestyle="-", linewidth=2, label="sparse (m = 4n)"))
    handles.append(plt.Line2D([0], [0], color="#555555", linestyle="--", linewidth=2, label="dense (m = 20n)"))
    if outside:
        ax.legend(handles=handles, loc="upper left", bbox_to_anchor=(1.02, 1.0), frameon=False, fontsize=9)
    else:
        ax.legend(handles=handles, loc=loc, frameon=False, fontsize=9)


def plot_operations(rows):
    fig, ax = plt.subplots(figsize=(8, 5.4), dpi=150)
    for a in ALGOS:
        for d in ("sparse", "dense"):
            pts = series(rows, a, d)
            xs = [p["n"] for p in pts]
            ys = [p["dsu_ops"] + p["edge_scans"] + p["comparisons"] for p in pts]
            ax.plot(xs, ys, color=COLORS[a], linestyle=STYLE[d], marker=MARKERS[a],
                    markersize=5, linewidth=1.8, markeredgecolor="white", markeredgewidth=0.8)
    ax.set_xscale("log")
    ax.set_yscale("log")
    style_axes(ax, "Operations vs n", "n (vertices)", "operations = dsu_ops + edge_scans + comparisons")
    legend_both(ax, "upper left")
    fig.tight_layout()
    fig.savefig(os.path.join(PLOT_DIR, "operations_vs_n.png"))
    plt.close(fig)


def plot_scans_per_edge(rows):
    fig, ax = plt.subplots(figsize=(10, 5.4), dpi=150)
    for a in ALGOS:
        for d in ("sparse", "dense"):
            pts = series(rows, a, d)
            xs = [p["n"] for p in pts]
            ys = [p["edge_scans"] / p["m"] for p in pts]
            ax.plot(xs, ys, color=COLORS[a], linestyle=STYLE[d], marker=MARKERS[a],
                    markersize=5, linewidth=1.8, markeredgecolor="white", markeredgewidth=0.8)
    ax.set_xscale("log")
    ax.set_ylim(0, 2.4)
    style_axes(ax, "Scans per edge", "n (vertices)", "edge_scans / m")
    legend_both(ax, None, outside=True)
    fig.tight_layout()
    fig.savefig(os.path.join(PLOT_DIR, "scans_per_edge.png"))
    plt.close(fig)


def plot_kruskal_share(rows):
    fig, ax = plt.subplots(figsize=(8, 5.0), dpi=150)
    for d, color in (("sparse", "#0072B2"), ("dense", "#D55E00")):
        pts = series(rows, "Kruskal", d)
        xs = [p["n"] for p in pts]
        ys = [p["edge_scans"] / p["m"] for p in pts]
        ax.plot(xs, ys, color=color, marker="o", markersize=6, linewidth=2,
                markeredgecolor="white", markeredgewidth=0.8, label=d + (" (m = 4n)" if d == "sparse" else " (m = 20n)"))
        for x, y in zip(xs, ys):
            ax.annotate(f"{y:.2f}", (x, y), textcoords="offset points", xytext=(0, 8),
                        ha="center", fontsize=8, color="#444444")
    ax.set_xscale("log")
    ax.set_ylim(0, 1.15)
    style_axes(ax, "Share of edges Kruskal examines", "n (vertices)", "edge_scans / m")
    ax.legend(frameon=False, loc="center right")
    fig.tight_layout()
    fig.savefig(os.path.join(PLOT_DIR, "kruskal_edge_share.png"))
    plt.close(fig)


def slope(xs, ys):
    lx = [math.log(x) for x in xs]
    ly = [math.log(y) for y in ys]
    mx = sum(lx) / len(lx)
    my = sum(ly) / len(ly)
    num = sum((a - mx) * (b - my) for a, b in zip(lx, ly))
    den = sum((a - mx) ** 2 for a in lx)
    return num / den


def analysis(rows):
    lines = []
    lines.append("Slope of edge_scans against m on log-log axes (5 points per line)")
    for a in ALGOS:
        for d in ("sparse", "dense"):
            pts = series(rows, a, d)
            s = slope([p["m"] for p in pts], [p["edge_scans"] for p in pts])
            lines.append(f"  {a:16s} {d:7s} slope = {s:.3f}")
    lines.append("")
    lines.append("Slope of comparisons against m (log-log)")
    for a in ("Kruskal", "Prim"):
        for d in ("sparse", "dense"):
            pts = series(rows, a, d)
            s = slope([p["m"] for p in pts], [p["comparisons"] for p in pts])
            lines.append(f"  {a:16s} {d:7s} slope = {s:.3f}")
    lines.append("")
    lines.append("Counter divided by formula")
    header = f"  {'algorithm':16s} {'density':8s} {'n':>7s}  " + "  ".join(
        f"{h:>14s}" for h in ("ratio1", "ratio2", "ratio3"))
    lines.append("  Kruskal: ratio1 = dsu_ops/E, ratio2 = dsu_ops/edge_scans, ratio3 = comparisons/(E*log2 E)")
    lines.append("  Prim: ratio1 = edge_scans/(2E), ratio2 = comparisons/(E*log2 E), ratio3 = comparisons/(2E*log2 2E)")
    lines.append("  Kosaraju: ratio1 = edge_scans/(V+E), ratio2 = edge_scans/(2E)")
    lines.append("  Kahn: ratio1 = edge_scans/(V+E), ratio2 = edge_scans/E")
    lines.append("  DagShortestPath: ratio1 = edge_scans/(V+E), ratio2 = comparisons/E")
    lines.append(header)
    for a in ALGOS:
        for d in ("sparse", "dense"):
            for p in series(rows, a, d):
                n, e = p["n"], p["m"]
                if a == "Kruskal":
                    vals = (p["dsu_ops"] / e, p["dsu_ops"] / p["edge_scans"],
                            p["comparisons"] / (e * math.log2(e)))
                elif a == "Prim":
                    vals = (p["edge_scans"] / (2 * e), p["comparisons"] / (e * math.log2(e)),
                            p["comparisons"] / (2 * e * math.log2(2 * e)))
                elif a == "Kosaraju":
                    vals = (p["edge_scans"] / (n + e), p["edge_scans"] / (2 * e), float("nan"))
                elif a == "Kahn":
                    vals = (p["edge_scans"] / (n + e), p["edge_scans"] / e, float("nan"))
                else:
                    vals = (p["edge_scans"] / (n + e), p["comparisons"] / e, float("nan"))
                lines.append(f"  {a:16s} {d:8s} {n:7d}  " + "  ".join(f"{v:14.4f}" for v in vals))
    lines.append("")
    lines.append("Kruskal share of edges examined")
    for d in ("sparse", "dense"):
        for p in series(rows, "Kruskal", d):
            lines.append(f"  {d:7s} n={p['n']:7d} edge_scans/m = {p['edge_scans'] / p['m']:.4f}")
    lines.append("")
    lines.append("Median time in ms")
    for a in ALGOS:
        for d in ("sparse", "dense"):
            pts = series(rows, a, d)
            lines.append(f"  {a:16s} {d:7s} " + "  ".join(f"{p['time_ms']:9.2f}" for p in pts))
    with open(ANALYSIS_PATH, "w") as f:
        f.write("\n".join(lines) + "\n")
    print("\n".join(lines))


def main():
    if not os.path.exists(CSV_PATH):
        sys.exit("results/results.csv not found, run the benchmark first")
    os.makedirs(PLOT_DIR, exist_ok=True)
    rows = load()
    plot_operations(rows)
    plot_scans_per_edge(rows)
    plot_kruskal_share(rows)
    analysis(rows)


if __name__ == "__main__":
    main()
