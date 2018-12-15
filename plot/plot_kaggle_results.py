import pandas as pd
import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt

mpl.rcParams['agg.path.chunksize'] = 10000

data = pd.read_csv("../results/kaggle/r_3_ss_1000_0.01_0.90.txt", delim_whitespace=True)

df = data.groupby("id").min()["cost"].value_counts().reset_index()
df.columns = ["cost", "quantity"]
df.plot(kind="bar", x="cost", y="quantity", title="Efectividad Sudokus 3x3", color="lightblue", legend=False)
plt.xlabel('repetitions')
plt.ylabel('quantity')
plt.ylim(0, 250)
plt.show()