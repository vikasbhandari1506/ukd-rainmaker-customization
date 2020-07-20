import pandas as pd
import os
import xlsxwriter
dirs=os.listdir(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\uk")
print(dirs)
for dir in dirs:
    try:
        files=os.listdir(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\uk"+"\\"+dir)
        for file in files:
            if len(file.split("."))==2:
                df=pd.read_json(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\uk"+"\\"+dir+"\\" +file)
                print(file)
                print(df)
                fileName = file.split(".")
                f = fileName[0]
                new_df=df[f]
                columns=[]
                values=[]
                max=0

                #This snippet gtives you the columns names
                for i in range(len(new_df)):
                    current=new_df[i]
                    l=len(list(current.keys()))
                    if l>max:
                        max=l
                        columns=list(current.keys())

                #This code snippet gives us actual values to be populated
                for i in range(len(new_df)):
                    current=new_df[i]
                    x=[]
                    for key in current.keys():
                        x.append(current[key])
                    if len(columns)>len(x):
                        count=len(columns)-len(x)
                        for j in range(count):
                            x.append("")
                    values.append(x)

                #This code snippet adds the values to the dataframe
                data=pd.DataFrame()
                for i in range(len(values[0])):
                    temp=[]
                    for val in values:
                        temp.append(val[i])
                    data[columns[i]]=temp

                os.mkdir(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel"+ "\\"+dir)
                data.to_excel(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel" + "\\" + dir + "\\" + f+ ".xlsx",
                            engine='xlsxwriter')

            else:
                nestedDirs=os.listdir(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\uk"+"\\"+dir+"\\"+file)
                os.mkdir(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel"+"\\"+dir)
                for nestedDir in nestedDirs:
                    print(nestedDir)
                    os.mkdir( r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel" + "\\" + dir+"\\"+ file)
                    df2=pd.read_json(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\uk"+"\\"+dir+"\\"+file+"\\"+nestedDir)
                    fN = nestedDir.split(".")[0]
                    if fN=="boundary-data":
                        fN="TenantBoundary"
                    new_df2 = df2[fN]
                    columns2 = []
                    values2 = []
                    max2 = 0


                    # This snippet gtives you the columns names
                    for i in range(len(new_df2)):
                        current2 = new_df2[i]
                        l = len(list(current2.keys()))
                        if l > max2:
                            max2 = l
                            columns2 = list(current2.keys())

                    # This code snippet gives us actual values to be populated
                    for i in range(len(new_df2)):
                        current2 = new_df2[i]
                        x2 = []
                        for key in current2.keys():
                            x2.append(current2[key])
                        if len(columns2) > len(x2):
                            count2 = len(columns2) - len(x2)
                            for j in range(count2):
                                x2.append("")
                        values2.append(x2)

                    # This code snippet adds the values to the dataframe
                    data2 = pd.DataFrame()
                    for i in range(len(values2[0])):
                        temp2 = []
                        for val in values2:
                            temp2.append(val[i])
                        data2[columns2[i]] = temp2

                    data2.to_excel(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel"+"\\"+dir+"\\"+file+"\\"+fN+".xlsx")
    except:
        continue