
import os
import math
from scipy import stats
import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import rc
import numpy as np


import csv


with open('2DormCorr.csv', 'r') as csvfile:
    fileReader = csv.reader(csvfile, delimiter=',')
    nameToVisitTimeMap = {}
    currentName= ""
    startValue=0
    for row in fileReader:
        for num,word in enumerate(row):
            if num == 0:
                nameToVisitTimeMap[word] = []
                currentName= word
                startValue=0
            else:
                value = long(word)/1000.0
                if startValue == 0:
                    startValue = value
                nameToVisitTimeMap[currentName].append(value-startValue)


    # fig = plt.figure(num=None, figsize=(50, 12), dpi=80)
     
    for name in nameToVisitTimeMap.keys():
        plt.plot(nameToVisitTimeMap.get(name),range(0,len(nameToVisitTimeMap[name]),1))
    plt.xlim(0,2100)
    plt.yticks(np.arange(0, 7, 1))
    plt.xticks(np.arange(0, 2100, 50))
    # # for tl in plt.get_yticklabels():
    # #     tl.set_color('r')
    # font = {'size': 30}


    # rc('font', **font)
    # # for tick in mpl.axis.Axis.get_major_ticks():
    # #     tick.label.set_fontsize(30);
    # # for tick in mpl.axis.YAxis.get_major_ticks():
    # #     tick.label.set_fontsize(30);
    # plt.tick_params(axis='both', which='major', labelsize=30)
    # # handles, labels = plt.get_legend_handles_labels()

    # # # reverse the order
    # # plt.legend(handles[::-1], labels[::-1])

    # # or sort them by labels
    
    # plt.ylabel("Total Number of Hotspots", 
    #    fontsize=30,
    #    verticalalignment='center',
    #    horizontalalignment='right',
    #    rotation='vertical' )
    # plt.xlabel("Percentage Trusting Device", 
    #    fontsize=30)
    # # print labels2
    # # plt.legend(handles2, labels2, loc="upper right")
    # # plt.legend(loc="upper right")
    plt.show()
    
    
    # plt.savefig("TrustProb-Scenario{0}".format(scenarioNumber), pad_inches=0)
    # plt.close()