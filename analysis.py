import csv
import matplotlib as mpl
import matplotlib.pyplot as plt
import statistics as stat
import numpy as np
import matplotlib.pyplot as plt
import math
from scipy.stats import chi2_contingency
from dateutil.parser import parse
from datetime import datetime

# initializing the score buckets
pos1_1 = []
pos2_1 = []
pos3_1 = []
pos4_1 = []

neg1_1 = []
neg2_1 = []
neg3_1 = []
neg4_1 = []

flag = True

yearAgo = datetime(2017, 4, 1, 0, 0)
count = 0

prj = "PROJECT_NAME"

# loop through the CSV. each row will be added to a positve score bucket and negative score bucket.
with open('../input.csv') as file:
    reader = csv.reader(file)
    for row in reader:
        
        if(flag):
            flag = False
            continue

        if(parse(row[1]) > yearAgo):
            continue


        if(row[0].split('-')[0] != prj):
            continue

        count = count+1

        if(row[6] == '1'):
            pos1_1.append(row)
        elif (row[6] == '2'):
            pos2_1.append(row)
        elif (row[6] == '3'):
            pos3_1.append(row)
        elif (row[6] == '4'):
            pos4_1.append(row)


        if(row[7] == '-1'):
            neg1_1.append(row)
        elif (row[7] == '-2'):
            neg2_1.append(row)
        elif (row[7] == '-3'):
            neg3_1.append(row)
        elif (row[7] == '-4'):
            neg4_1.append(row)
              

scores = [[pos1_1, pos2_1, pos3_1, pos4_1], [neg1_1, neg2_1, neg3_1, neg4_1]]   

positive = True

# creating chi-squared tables and graphs for number of reopenings
for x in range(0, 2):

    arr = scores[x]
    
    graph1 = [0, 0, 0, 0]
    graph2 = [0, 0, 0, 0]
    graph3 = [0, 0, 0, 0]

    chi1 = [0, 0, 0, 0]
    chi2 = [0, 0, 0, 0]
    chi3 = [0, 0, 0, 0]

    nones = 0
    ones = 0
    manys = 0

    # loop through score bucket and make note of how many times it has been reopened
    for i in range(0, 4):

        # some projects didn't have any results for a particular score. assign as 0 and move on
        if(len(arr[i]) == 0):
            graph1[i] = 0
            graph2[i] = 0
            graph3[i] = 0
            continue

        none = 0
        one = 0
        many = 0

        # checking score and updating tally
        for row in arr[i]:
            if(row[3] == '0'):
                none += 1
            elif(row[3] == '1'):
                one += 1
            else:
                many += 1

        chi1[i] = none
        chi2[i] = one
        chi3[i] = many

        nones += none
        ones += one
        manys += many

        # calculating percentages
        nums = [none, one, many]
        percents = [x / (len(arr[i])) for x in nums]
        
        graph1[i] = percents[0]*100
        graph2[i] = percents[1]*100
        graph3[i] = percents[2]*100

    #creating the graphs from the data
    N = 4
    ind = np.arange(N)    # the x locations for the groups
    width = 0.35       # the width of the bars: can also be len(x) sequence

    p3 = plt.bar(ind, graph3, width, color="blue", bottom= np.array(graph2) + np.array(graph1))
    p2 = plt.bar(ind, graph2, width, color="green", bottom= np.array(graph1))
    p1 = plt.bar(ind, graph1, width, color="red")

    for i, rect in enumerate(p1.patches):
        plt.text(rect.get_x() + rect.get_width()/2, 105,
                '%d' % len(scores[x][i]), 
                ha='center', va='bottom', weight='bold')

    plt.ylabel('Percentage')
    plt.title('Percents for each score', y=1.08)
    
    if x%2 == 0:
        plt.xticks(ind, ('1', '2', '3', '4'))
    else:
        plt.xticks(ind, ('-1', '-2', '-3', '-4'))


    legendNone = "None: %s" % nones
    legendOne = "One: %s" % ones
    legendMany = "Many: %s" % manys
    plt.legend((p1, p2, p3), (legendNone, legendOne, legendMany), loc=3)

    if(x == 0 or x == 1):
        label = "low"
    elif(x == 2 or x == 3):
        label = "med"
    else:
        label = "high"

    if(positive):
        plt.savefig("positive_ALL" + str(x) + label + ".png")
        positive = False
    else:
        plt.savefig("negative_ALL" + str(x) + label + ".png")
        positive = True
        

    print("Total number of issues:" + str(count))
    tab = np.array([list(a) for a in zip(chi1, chi2, chi3)])
    table = np.array([chi1, chi2, chi3])
    stats = chi2_contingency(tab, correction=False)
    print(stats)

    print()
    print("Counts:")
    print(tab)

    chi2 = stats[0]
    df = min(tab.shape[0]-1, tab.shape[1]-1)
    V = math.sqrt(chi2 / (df*count))
    print()
    print("Cramer's V: " + str(V))
    print("Degrees of freedom: " + str(df))
    print()

    plt.clf()