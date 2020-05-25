#!/bin/env python3

import matplotlib.pyplot as plt
import numpy as np
import sys

def get_tps_from_siege_out(filename):
    with open(filename) as f:
        lines = f.readlines()
    for line in lines:
        if "Transaction rate" in line:
            splits = line.split()
            return float(splits[2])
    return None

def grouped_bar_plot(series, series_labels, groups_labels):
    # set width of bar
    barWidth = 1/(len(series)+1)
    r = []
    for label, s in zip(series_labels, series):
        if not r:
            r.append(np.arange(len(s)))
        else:
            r.append([x + barWidth for x in r[-1]])
        
        plt.bar(r[-1], s, width=barWidth, label=label)
 
    # Add xticks on the middle of the group bars
    plt.xlabel('Query type', fontweight='bold')
    plt.ylabel('Throughput (req/s)', fontweight='bold')
    plt.xticks([r + (len(series)-1)/2 * barWidth for r in range(len(series[0]))], groups_labels)

    plt.gca().set_yticks(np.arange(0,np.max(series), 10))
 
    # Create legend & Show graphic
    plt.legend()
    plt.grid(which="both", axis="y")
    plt.show()

series = [('Filtered Browse', 'browse_complex'), ('Simple Statistics', 'statistics_simple'), ('Filtered Statistics', 'statistics_complex')]
# series = [('Simple Browse', 'browse_simple'), ('Filtered Browse', 'browse_complex'), ('Search', 'search'), ('Details', 'details'), ('Simple Statistics', 'statistics_simple'), ('Filtered Statistics', 'statistics_complex')]

if __name__ == '__main__':
    d = []
    for i in range(1,4):
        di = []
        for label, name in series:
            di.append(get_tps_from_siege_out(f"{sys.argv[1]}/{name}_urls.txt.siege.out.{i}"))
        d.append(di)


    grouped_bar_plot(d, ["%d servers" % i for i in range(1,4)], [s[0] for s in series]) 