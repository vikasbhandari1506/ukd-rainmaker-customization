import json
import os
import pandas as pd
from collections import OrderedDict

# Validate the masters against master config
# with open('./data/mdms-masters-config.json') as f:
#   data = json.load(f)

# masters =[]
# for module in data.keys():
#     masters.extend(data[module].keys())
# # print(masters)


def process_list(key, values, preprocessed_data):
    if isinstance(values[0], str) or isinstance(values[0], int) or isinstance(values[0], float) or isinstance(values[0], bool) or values[0] == None:
        preprocessed_data[key + "__list"] = ','.join(values)
    elif isinstance(values[0], list) or isinstance(values[0], tuple):
        for value in values:
            preprocess_data(key + "__list", value,  preprocessed_data)
    elif isinstance(values[0], dict):
        temp_data = OrderedDict({})
        for value in values:
            for temp_i, temp_j in value.items():
                temp_data[key+'__list'+'__'+temp_i] = temp_j
            preprocess_data(temp_data, preprocessed_data)


def preprocess_data(data, preprocessed_data):
    # data is list so we need bring the objects in list to same format and then process below on the all objects
    for i, j in data.items():
        if isinstance(j, str) or isinstance(j, int) or isinstance(j, float) or isinstance(j, bool) or j == None:
            preprocessed_data[i] = j
        elif isinstance(j, list) or isinstance(j, tuple):
            if len(j) > 0:
                if isinstance(j[0], str) or isinstance(j[0], int) or isinstance(j[0], float) or isinstance(j[0], bool) or j[0] == None:
                    #process_list(i,j, preprocessed_data)
                    preprocessed_data[i + "__list"] = ','.join(j)
                else:
                    preprocessed_data[i + "__list"] = json.dumps(j)
            else:
                preprocessed_data[i + "__list"] = ''
        elif isinstance(j, dict):
            temp_data = OrderedDict({})
            for temp_i, temp_j in j.items():
                temp_data[i+'__'+temp_i] = temp_j
            preprocess_data(temp_data, preprocessed_data)


def reverse_preprocess_dict(data, processed_data):
    for i, j in data.items():
        if "__" not in i:
            processed_data[i] = j
        elif "__list" in i:
            key = i.replace("__list", "")
            if j == '' or j == None:
                processed_data[key] = []
            elif j[0] == '[':
                processed_data[key] = json.loads(j) if len(j) < 32765 else j
            else:
                processed_data[key] = j.split(",")
        elif "__" in i:
            keys = i.split("__")
            parent = processed_data
            for key in keys[:-1]:
                if key not in parent:
                    parent[key] = {}
                parent = parent[key]
            parent[keys[-1]] = j

    return data


def convert_json_to_excel(oldpath, newpath):
    filename = os.path.basename(oldpath)
    print('Converting ', filename)
    with open(oldpath) as f:
        data = json.load(f, object_pairs_hook=OrderedDict)
    tenantId = data['tenantId']
    moduleName = data['moduleName']
    filename = filename.split(".")[0]

    data.pop('tenantId', None)
    data.pop('moduleName', None)
    if len(data.keys()) == 1:
        # convert
        data_to_convert = data[list(data.keys())[0]]

        # preprocess the dict to get our required format
        data_after_preprocessed = []
        for item in data_to_convert:
            data_after_preprocess = OrderedDict({})
            preprocess_data(item, data_after_preprocess)
            data_after_preprocessed.append(data_after_preprocess)

        df = pd.DataFrame.from_dict(data_after_preprocessed)
        #print (list(df.columns.values))
        writer = pd.ExcelWriter(
            newpath+'/'+tenantId+'_'+moduleName+'_'+filename+'.xlsx', engine='xlsxwriter')
        df.to_excel(writer, sheet_name='Sheet1', index=False)
        writer.save()

    else:
        print(oldpath, " having more than one master ")


def convert_excel_to_json(oldpath, newpath):
    filename = os.path.basename(oldpath)
    print('Converting ', filename)
    master = filename.replace(".xlsx", "")

    tenantId = master.split("_")[0]
    moduleName = master.split("_")[1]
    master = master.split("_")[2]

    df = pd.read_excel(oldpath, sheet_name='Sheet1')
    data = json.loads(df.to_json(orient='records'))
    # reversing the preprocess

    data_after_reversed = []
    for item in data:
        data_after_reverse = OrderedDict({})
        reverse_preprocess_dict(item, data_after_reverse)
        data_after_reversed.append(data_after_reverse)

    output = {
        "tenantId": tenantId,
        "moduleName": moduleName,
        master: data_after_reversed
    }

    with open(newpath+'/'+master+'.json', 'w') as outfile:
        json.dump(output, outfile, indent=4)


def convert_all(src, dest, json_to_excel):
    if not os.path.exists(dest):
        os.mkdir(dest)
    for item in os.listdir(src):
        file_path = os.path.join(src, item)

        # if item is a file, copy it
        if os.path.isfile(file_path):
            if json_to_excel:
                convert_json_to_excel(file_path, dest)
            else:
                convert_excel_to_json(file_path, dest)

        # else if item is a folder, recurse
        elif os.path.isdir(file_path):

            new_dest = os.path.join(dest, item)
            if not os.path.exists(new_dest):
                os.mkdir(new_dest)
            convert_all(file_path, new_dest, json_to_excel)


# convert_all(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\uk",r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel", True)
# convert_all(r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukexcel",r"C:\Users\rshre\Downloads\ukd-mdms-data-master (1)\ukd-mdms-data-master\data\ukjson", False)
convert_json_to_excel(r"./test1.json",r"./")
convert_excel_to_json(r"./uk_tenant_test1.xlsx", "./converted")
