import pandas as pd
import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt

data_files = [
    ("../results/params_fixation/r_4_ao_1000_0.10_0.90.txt", "Estrategia 1, Tmin = 0.1"),
    ("../results/params_fixation/r_4_sb_1000_0.10_0.90.txt", "Estrategia 2, Tmin = 0.1"),
    ("../results/params_fixation/r_4_ss_1000_0.10_0.90.txt", "Estrategia 3, Tmin = 0.1"),
    ("../results/params_fixation/r_4_ao_1000_0.01_0.90.txt", "Estrategia 1, Tmin = 0.01"),
    ("../results/params_fixation/r_4_sb_1000_0.01_0.90.txt", "Estrategia 2, Tmin = 0.01"),
    ("../results/params_fixation/r_4_ss_1000_0.01_0.90.txt", "Estrategia 3, Tmin = 0.01")
]

mpl.rcParams['agg.path.chunksize'] = 10000

for file, title in data_files:
    data = pd.read_csv(file, delim_whitespace=True)
    
    fig, ax = plt.subplots()
    ax.set_ylim(0, 1000)
    for i in range(1, 51):
        df = data[data.id == i]
        df.plot(x="iteration", y="cost", c="orange", alpha=0.1, ax=ax)
    ax.get_legend().remove()
    ax.set_ylabel("repetitions")
    fig.suptitle(title)
    plt.show()