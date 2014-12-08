
import os
import math
from scipy import stats
import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import rc
import numpy as np


import csv


with open('2DormCorr-Limited.csv', 'r') as csvfile:
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
                value = long(word)/10000.0
                if startValue == 0:
                    startValue = value
                nameToVisitTimeMap[currentName].append(value-startValue)
    

    interVisitTimes= []
    for name in nameToVisitTimeMap.keys():
        lastStartTime = 0 
        for visitTime in nameToVisitTimeMap.get(name):
            interVisitTimes.append(int(visitTime-lastStartTime))
            lastStartTime = visitTime
    # print len(interVisitTimes)
    print sorted(interVisitTimes)

    plt.hist(interVisitTimes);
    plt.show()

    # fig = plt.figure(num=None, figsize=(50, 12), dpi=80)
     
    # for name in nameToVisitTimeMap.keys():
    #     plt.plot(nameToVisitTimeMap.get(name),range(0,len(nameToVisitTimeMap[name]),1))
    # plt.xlim(0,15)
    # plt.yticks(np.arange(0, 8, 1))
    # plt.xticks(np.arange(0, 15, 1))
    
    # plt.show()
    