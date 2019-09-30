import matplotlib
import matplotlib.pyplot as plt
from time import sleep
from subprocess import Popen, PIPE, STDOUT
from IPython.display import clear_output, display
import urllib.parse
from io import BytesIO
from zipfile import ZipFile
import urllib.request
import datetime
import os
from subprocess import Popen, PIPE, STDOUT
import pandas
import numpy
from IPython.display import display, HTML

def readDataAndGenerateCSV(scenario):
	if scenario == 1 or scenario == 2:
	    zNormalized = 1
	if scenario == 3:
	    zNormalized = 0

	p = Popen(['java', '-jar', 'StockDataGenerateCSV-1.0-jar-with-dependencies.jar', 'properties_scenario' + str(scenario) + '.conf'], stdout=PIPE, stderr=STDOUT)
	for line in p.stdout:
	        if line[2] == 115:
	            print(str(line)[2:-3])
	            sleep(2)
	        if line[2] == 101:
	            clear_output(wait=True)
	            print(str(line)[2:-3])
	        else:
	            print(str(line)[2:-3])

	if zNormalized == 1:
	    stocks_file = open("stockTS.csvzNorm.csv")
	else:
	    stocks_file = open("stockTS.csv")
	stocks_lines = stocks_file.readlines()

	stocks = {}
	for s in stocks_lines:
	    stock_name = s.split(",")[0]
	    stock_values_str = s.split("\n")[0].split(",")[3:264]
	    stock_values = []
	    for st in stock_values_str:
	        stock_values.append(float(st))
	    stocks[stock_name] = stock_values
	return stocks

def discoverBundles(scenario):
	if scenario > 0:
	    p = Popen(['java', '-jar', 'BundleDiscovery-1.0-jar-with-dependencies.jar', 'properties_scenario' + str(scenario) + '.conf'], stdout=PIPE, stderr=STDOUT)
	    for line in p.stdout:
	        if line[2] == 115:
	            print(str(line)[2:-3])
	            sleep(2)
	        if line[2] == 101:
	            clear_output(wait=True)
	            print(str(line)[2:-3])
	        else:
	            print(str(line)[2:-3])
	else:
	    p = Popen(['java', '-jar', './GET_DB_DATA.jar'], stdout=PIPE, stderr=STDOUT)
	    for line in p.stdout:
	        if line[2] == 115:
	            print(str(line)[2:-3])
	            sleep(2)
	        if line[2] == 101:
	            clear_output(wait=True)
	            print(str(line)[2:-3])
	        else:
	            print(str(line)[2:-3])


def plotAllData(stock_market, year, stocks):
	xlim_1 = 0
	xlim_2 = 261

	plt.rcParams['figure.figsize'] = [28, 14]
	plt.title('Stocks of ' + stock_market + '\'s Stock Market', fontsize=35)
	plt.ylabel('Close Value', fontsize=35)
	plt.xlabel('Working Day of Year ' + year, fontsize=35)
	plt.xticks(fontsize=25)
	plt.yticks(fontsize=25)
	axes = plt.gca()
	axes.set_xlim([xlim_1, xlim_2])

	plt.grid(True)
	for k, v in stocks.items():
	    plt.plot(v)

	#return bundles_members, bundles_duration

def plotAllBundles(stock_market, year, stocks):
	results_file = open("results.txt","r")
	results_lines = results_file.readlines()

	xlim_1 = 0
	xlim_2 = 261

	bundles_members = {}
	bundles_duration = {}
	for b in results_lines:
	    bundle_name = b.split(";")[0]
	    duration = b.split(";")[2]
	    #members = b.split("\n")[0].split(":")[1].split(",")
	    members = b.split(";")[1].split(",")
	    bundles_members[bundle_name] = members
	    bundles_duration[bundle_name] = duration

	for k, v in bundles_members.items():
	    duration = bundles_duration[k]

	    x1 = int(duration.split("-")[0][1:])
	    x2 = int(duration.split("-")[1][:-2])

	    minimum = []
	    maximum = []
	    for i in list(range(x2-x1)):
	        minimum.append(10000000000)
	        maximum.append(-1)
	        
	    plt.rcParams['figure.figsize'] = [28, 14]
	    plt.title('Discovered Bundles of Stocks (' + stock_market + '\'s Stock Market)', fontsize=35)
	    plt.ylabel('Close Value', fontsize=35)
	    plt.xlabel('Working Day of Year ' + year, fontsize=35)
	    plt.xticks(fontsize=25)
	    plt.yticks(fontsize=25)
	    axes = plt.gca()
	    axes.set_xlim([xlim_1, xlim_2])
	    
	    for member in bundles_members[k]:
	        ts = stocks[member]
	        idx = 0
	        for t in list(range(x1, x2)):
	            if ts[t] < minimum[idx]:
	                minimum[idx] = ts[t]
	            if ts[t] > maximum[idx]:
	                maximum[idx] = ts[t]
	            idx += 1
	            
	    p = plt.plot(list(range(x1, x2)), maximum, linewidth=5.0)
	    plt.plot(list(range(x1, x2)), minimum, color=p[0].get_color(), linewidth=5.0)
	    plt.grid(True)
	    plt.fill_between(list(range(x1, x2)), minimum, maximum, color=p[0].get_color(), alpha=0.15)

def plotSelectedBundle(scenario, bundle_to_visualize, stock_market, year, stocks):
	results_file = open("results.txt","r")
	results_lines = results_file.readlines()

	xlim_1 = 0
	xlim_2 = 261

	bundles_members = {}
	bundles_duration = {}
	for b in results_lines:
	    bundle_name = b.split(";")[0]
	    duration = b.split(";")[2]
	    #members = b.split("\n")[0].split(":")[1].split(",")
	    members = b.split(";")[1].split(",")
	    bundles_members[bundle_name] = members
	    bundles_duration[bundle_name] = duration
	
	if bundle_to_visualize == -1:
		if scenario == 1 or scenario == 2:
		    bundle_to_visualize = 'Bundle_0'
		if scenario == 3:
		    bundle_to_visualize = 'Bundle_100'
	else:
			bundle_to_visualize = bundle_to_visualize

	duration = bundles_duration[bundle_to_visualize]
	x1 = int(duration.split("-")[0][1:])
	x2 = int(duration.split("-")[1][:-2])

	minimum = []
	maximum = []
	for i in list(range(x2-x1)):
	    minimum.append(10000000000)
	    maximum.append(-1)

	plt.rcParams['figure.figsize'] = [28, 14]
	plt.title('Discovered Bundles of ' + bundle_to_visualize +  ' (' + stock_market + '\'s Stock Market)', fontsize=35)
	plt.ylabel('Close Value', fontsize=35)
	plt.xlabel('Working Day of Year ' + year, fontsize=35)
	plt.xticks(fontsize=25)
	plt.yticks(fontsize=25)
	axes = plt.gca()
	axes.set_xlim([xlim_1, xlim_2])

	print('BUNDLE MEMBERS:')
	for member in bundles_members[bundle_to_visualize]:
	    ts = stocks[member]
	    idx = 0
	    for t in list(range(x1, x2)):
	        if ts[t] < minimum[idx]:
	            minimum[idx] = ts[t]
	        if ts[t] > maximum[idx]:
	            maximum[idx] = ts[t]
	        idx += 1
	    plt.plot(ts)
	    print(member)
	    
	plt.axvline(x = x1)
	plt.axvline(x = x2)
	plt.plot(list(range(x1, x2)), maximum, color='#539ecd', linewidth=5.0)
	plt.plot(list(range(x1, x2)), minimum, color='#539ecd', linewidth=5.0)
	plt.grid(True)
	plt.fill_between(list(range(x1, x2)), minimum, maximum, color='#539ecd', alpha=0.25)

def segmentData(year, interval, symbol, zNormalize):
	if os.path.exists("singleStockTS.csv"):
		os.remove("singleStockTS.csv")

	count1 = 1
	count2 = 0
	string_to_write = ""
	for i in range(1,12):
		month = str(i)
		if i < 10:
			month = "0" + month
		link = "http://5.175.24.176/Qualimaster/history/" + str(interval) + "/" + year + month + "_" + symbol + ".zip"
		link = urllib.parse.urlsplit(link)
		link = list(link)
		link[2] = urllib.parse.quote(link[2])
		link = urllib.parse.urlunsplit(link)
		url = urllib.request.urlopen(link)
		with ZipFile(BytesIO(url.read())) as my_zip_file:
			for contained_file in my_zip_file.namelist():
				for line in my_zip_file.open(contained_file).readlines():
					line = line.decode("utf-8")
					date = line.split(",")[0]
					day_of_week = datetime.datetime.strptime(date, '%m/%d/%Y').strftime('%a')
					if day_of_week == "Mon" and prev_day_of_week == "Fri":
						if count2 == 45:
							with open('singleStockTS.csv', 'a') as the_file:
								the_file.write(string_to_write + "\n")
						count1 += 1
						string_to_write = "Week_" + str(count1) + ",X,Y"
						count2 = 0
					if count1>1 and count1<52:
						string_to_write += "," + str(float(line.split(",")[5]))
						count2 += 1
					prev_day_of_week = day_of_week

	p = Popen(['java', '-jar', 'StockDataGenerateCSV-1.0-jar-with-dependencies.jar', str(zNormalize), 'singleStockTS.csv', str(45)], stdout=PIPE, stderr=STDOUT)
	for line in p.stdout:
	        if line[2] == 115:
	            print(str(line)[2:-3])
	            sleep(2)
	        if line[2] == 101:
	            clear_output(wait=True)
	            print(str(line)[2:-3])
	        else:
	            print(str(line)[2:-3])

	if zNormalize == 1:
	    stocks_file = open("singleStockTS.csvzNorm.csv")
	else:
	    stocks_file = open("singleStockTS.csv")
	stocks_lines = stocks_file.readlines()

	stocks = {}
	for s in stocks_lines:
	    stock_name = s.split(",")[0]
	    stock_values_str = s.split("\n")[0].split(",")[3:264]
	    stock_values = []
	    for st in stock_values_str:
	        stock_values.append(float(st))
	    stocks[stock_name] = stock_values
	return stocks

def plotAllData2(symbol, year, stocks):
	xlim_1 = 0
	xlim_2 = 44

	plt.rcParams['figure.figsize'] = [28, 14]
	plt.title(symbol, fontsize=35)
	plt.ylabel('Close Value', fontsize=35)
	plt.xlabel('Working Hour of Week for Year ' + year, fontsize=35)
	plt.xticks(fontsize=25)
	plt.yticks(fontsize=25)
	axes = plt.gca()
	axes.set_xlim([xlim_1, xlim_2])

	plt.grid(True)
	for k, v in stocks.items():
	    plt.plot(v)

def plotAllBundles2(symbol, year, stocks):
	results_file = open("results.txt","r")
	results_lines = results_file.readlines()

	xlim_1 = 0
	xlim_2 = 44

	bundles_members = {}
	bundles_duration = {}
	for b in results_lines:
	    bundle_name = b.split(";")[0]
	    duration = b.split(";")[2]
	    #members = b.split("\n")[0].split(":")[1].split(",")
	    members = b.split(";")[1].split(",")
	    bundles_members[bundle_name] = members
	    bundles_duration[bundle_name] = duration

	for k, v in bundles_members.items():
	    duration = bundles_duration[k]

	    x1 = int(duration.split("-")[0][1:])
	    x2 = int(duration.split("-")[1][:-2])

	    minimum = []
	    maximum = []
	    for i in list(range(x2-x1)):
	        minimum.append(10000000000)
	        maximum.append(-1)
	        
	    plt.rcParams['figure.figsize'] = [28, 14]
	    plt.title('Discovered Bundles of ' + symbol, fontsize=35)
	    plt.ylabel('Close Value', fontsize=35)
	    plt.xlabel('Working Hour of Week for Year ' + year, fontsize=35)
	    plt.xticks(fontsize=25)
	    plt.yticks(fontsize=25)
	    axes = plt.gca()
	    axes.set_xlim([xlim_1, xlim_2])
	    
	    for member in bundles_members[k]:
	        ts = stocks[member]
	        idx = 0
	        for t in list(range(x1, x2)):
	            if ts[t] < minimum[idx]:
	                minimum[idx] = ts[t]
	            if ts[t] > maximum[idx]:
	                maximum[idx] = ts[t]
	            idx += 1
	            
	    p = plt.plot(list(range(x1, x2)), maximum, linewidth=5.0)
	    plt.plot(list(range(x1, x2)), minimum, color=p[0].get_color(), linewidth=5.0)
	    plt.grid(True)
	    plt.fill_between(list(range(x1, x2)), minimum, maximum, color=p[0].get_color(), alpha=0.15)

def plotSelectedBundle2(scenario, bundle_to_visualize, symbol, year, stocks):
	results_file = open("results.txt","r")
	results_lines = results_file.readlines()

	xlim_1 = 0
	xlim_2 = 44

	bundles_members = {}
	bundles_duration = {}
	for b in results_lines:
	    bundle_name = b.split(";")[0]
	    duration = b.split(";")[2]
	    #members = b.split("\n")[0].split(":")[1].split(",")
	    members = b.split(";")[1].split(",")
	    bundles_members[bundle_name] = members
	    bundles_duration[bundle_name] = duration

	duration = bundles_duration[bundle_to_visualize]
	x1 = int(duration.split("-")[0][1:])
	x2 = int(duration.split("-")[1][:-2])

	minimum = []
	maximum = []
	for i in list(range(x2-x1)):
	    minimum.append(10000000000)
	    maximum.append(-1)

	plt.rcParams['figure.figsize'] = [28, 14]
	plt.title('Discovered Bundles of ' + bundle_to_visualize, fontsize=35)
	plt.ylabel('Close Value', fontsize=35)
	plt.xlabel('Working Hour of Week for Year ' + year, fontsize=35)
	plt.xticks(fontsize=25)
	plt.yticks(fontsize=25)
	axes = plt.gca()
	axes.set_xlim([xlim_1, xlim_2])

	print('BUNDLE MEMBERS:')
	for member in bundles_members[bundle_to_visualize]:
	    ts = stocks[member]
	    idx = 0
	    for t in list(range(x1, x2)):
	        if ts[t] < minimum[idx]:
	            minimum[idx] = ts[t]
	        if ts[t] > maximum[idx]:
	            maximum[idx] = ts[t]
	        idx += 1
	    plt.plot(ts)
	    print(member)
	    
	plt.axvline(x = x1)
	plt.axvline(x = x2)
	plt.plot(list(range(x1, x2)), maximum, color='#539ecd', linewidth=5.0)
	plt.plot(list(range(x1, x2)), minimum, color='#539ecd', linewidth=5.0)
	plt.grid(True)
	plt.fill_between(list(range(x1, x2)), minimum, maximum, color='#539ecd', alpha=0.25)

def getSimilarBundles(sort_by):
	pandas.set_option('display.max_colwidth', -1)

	p = Popen(['java', '-jar', 'simjoin-0.0.1-SNAPSHOT-jar-with-dependencies.jar', 'ssjoin_config.properties'], stdout=PIPE, stderr=STDOUT)

	f = open('ssjoin_out.txt', "r")
	lines_ssjoin = f.readlines()
	f.close()

	f = open('results.txt', "r")
	lines_bundles = f.readlines()
	f.close()

	first_time = True
	for line in lines_ssjoin:
	    res_list = []
	    bundle1 = lines_bundles[int(line.split(",")[0])]
	    bundle1_name = bundle1.split(";")[0]
	    bundle1_members = bundle1.split(";")[1]
	    bundle1_interval_start = int(bundle1.split(";")[2].split("-")[0][1:])
	    bundle1_interval_end = int(bundle1.split(";")[2].split("-")[1][:-2])
	    
	    bundle2 = lines_bundles[int(line.split(",")[1])]
	    bundle2_name = bundle2.split(";")[0]
	    bundle2_members = bundle2.split(";")[1]
	    bundle2_interval_start = int(bundle2.split(";")[2].split("-")[0][1:])
	    bundle2_interval_end = int(bundle2.split(";")[2].split("-")[1][:-2])
	    
	    res_list.append(bundle1_name + " " + bundle2_name)
	    
	    similarity = float(line.split(",")[2].split("\n")[0][:-1])
	    res_list.append(similarity)

	    x = range(bundle1_interval_start, bundle1_interval_end+1)
	    x_len = bundle1_interval_end-bundle1_interval_start
	    y = range(bundle2_interval_start, bundle2_interval_end+1)
	    y_len = bundle2_interval_end-bundle2_interval_start
	    xs = set(x)
	    intersect_length = len(xs.intersection(y))
	    interval_similarity = 0
	    if x_len > y_len:
	        interval_similarity = intersect_length/x_len
	    else:
	        interval_similarity = intersect_length/y_len  
	    res_list.append(interval_similarity)
	    
	    bundle1_set = set(''.join(bundle1_members).split(","))
	    bundle2_set = set(''.join(bundle2_members).split(","))
	    
	    common = bundle1_set.intersection(bundle2_set)
	    res_list.append(common)
	    not_common = bundle1_set.symmetric_difference(bundle2_set)
	    res_list.append(not_common)

	    if first_time == True:
	        newArray = numpy.array(res_list)
	        first_time = False
	    else:
	        newArray = numpy.vstack([newArray, res_list])
	    
	field_list = ["Bundle Pair", "Member Similarity", "Interval Similarity", "Common Members", "Non-Common Members"]
	members_incr = range(1, len(newArray)+1)
	display(pandas.DataFrame(newArray, members_incr, field_list).sort_values(sort_by, ascending=False))