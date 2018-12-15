import pandas as pd
import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt

mpl.rcParams['agg.path.chunksize'] = 10000

data_files = [
    ("../results/difficulty/r_4_genius_ss_1000_0.01_0.90.txt", "Efectividad Sudokus 4x4, Dificultad Alta", "#5B5678"),
    ("../results/difficulty/r_5_genius_ss_1000_0.01_0.90.txt", "Efectividad Sudokus 5x5, Dificultad Alta", "#99CC33")
]

for file, title, color in data_files:
    data = pd.read_csv(file, delim_whitespace=True)

    df = data.groupby("id").min()["cost"].value_counts().reset_index()
    df.columns = ["cost", "quantity"]
    df.plot(kind="bar", x="cost", y="quantity", title=title, color=color, legend=False)
    plt.xlabel('repetitions')
    plt.ylabel('quantity')
    plt.ylim(0, 10)
    plt.show()