#!/bin/env python3

import matplotlib.pyplot as plt
import numpy as np
import sys

def extract_series(filename):
    with open(filename) as f:
        lines = f.readlines()

    x = []
    y = []

    for line in lines:
        if 'GET' in line:
            splits = line.split()
            x.append(float(splits[0]))
            y.append(float(splits[3]))
    
    return x, y

def moving_average_plot(x, y, window):
    xa = np.array(x)
    ya = np.array(y)

    wxa = []
    wya = []
    wmina = []
    wmaxa = []

    print("  0.00%", end='')
    for i in range(len(xa)):
        print("\r%6.2f%%" % (i/len(xa)*100), end='')
        if xa[i] < window:
            continue
        mask = (xa <= xa[i]) & (xa >= xa[i] - window)
        wxa.append(xa[i])
        wya.append(ya[mask].mean())
        wmina.append(np.percentile(ya[mask],5))
        wmaxa.append(np.percentile(ya[mask],95))

    print()
    
    plt.plot(wxa, wya)
    plt.fill_between(wxa, wmina, wmaxa, alpha=0.2)
    plt.axvline(60)
    plt.axvline(120)
    plt.axvline(180)
    plt.axvline(240)
    plt.gca().set_yticks(np.arange(0, max(wmaxa)*10, 5)/10)
    plt.gca().set_xticks(np.arange(0, max(xa), 10))
    plt.grid(axis='both', which='major')
    plt.xlabel('Elapsed time (s)', fontweight='bold')
    plt.ylabel('Response time (s)', fontweight='bold')
    plt.show()

if __name__ == "__main__":
    x, y = extract_series(sys.argv[1])
    moving_average_plot(x, y, int(sys.argv[2]))
