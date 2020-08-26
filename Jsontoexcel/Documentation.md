**DOCUMENTATION** :

The following code has been written to convert given Json files to Excel and vice versa.

This enables users who aren't well versed with Json to access the data and make necessary changes in the Excel format. This code creates automated workflows that parses JSON and reformats it into Excel tables. It allow you to easily map the contents of a JSON dataset into their proper column and row in Excel. You are also able to extract only the portions of the JSON that you need converted to XLS and to manipulate the contents so the output Excel table fits your exact requirements.

**JavaScript Object Notation**

JavaScript Object Notation, or JSON, is an open standard data interchange format that is most commonly used to transmit data between servers and web applications as an alternative to XML.

**Excel**

Microsoft Excel is considered to be the industry standard for spreadsheet applications featuring calculation and graphing tools for data analysis and reporting.

**Flow of program:**

Once the JSON , OS and pandas are imported, we define the functions.

## API

**preprocess_data:**

This function takes data and preprocessed data as its arguments. It then checks if the first instance is a string, integer, float ,Boolean or any value

And it then takes the pre-processed data in a dictionary form and converts it into a list.

```
Data
{
    "time": "12AM",
    "details":{
        "hours":"12",
        "minute":"12",
        "second":"12"
    }
    "detailsInList":[12,12,12]
}

preprocess_data
{
    "time": "12AM",
    "details__hours":"12",
    "details__minute":"12",
    "details__second":"12"
    "detailsInList__list": "12,12,12"
}

```

**reverse_preprocess_dict:**

This function is used for reverse conversion from Excel to JSON performed in a similar manner. Here it also checks if each Excel cell has value less than 32765, which is the maximum value that an Excel cell can accommodate.

```
Data
{
    "time": "12AM",
    "details__hours":"12",
    "details__minute":"12",
    "details__second":"12"
    "detailsInList__list": "12,12,12"
}

processed_data
{
    "time": "12AM",
    "details":{
        "hours":"12",
        "minute":"12",
        "second":"12"
    },
    "detailsInList":[12,12,12]
}


```

**convert_json_to_excel:**

This function reads the JSON files and converts to excel and save the file in given location.

##### Parameters

- oldpath - source json file
- newfile - location to save excel file

If length of data keys is equal to 1 the conversion starts. Here we are checking if if the file has more than one master. If it does a message is printed. In the conversion block the data is converted from dictionaries to data frame and the file is renamed with a new path_tenantId\_ module name\_ file name with xlsx extension.

The file is saved.

**convert_excel_to_json:**

This function reads the Excel files and converts them to JSON and save the file in given location.

##### Parameters

- oldpath - source excel file
- newfile - location to save json file

**convert:**

##### Parameters

- source - source excel/json file
- newfile - location to save the converted file
- json_to_excel- this argument checks for the direction of conversion to be followed.

It joins the source with the item. It uses the join function. The join () method provides a flexible way to create string from iterable objects. Is joins each element of an iterable such as list, string and tuple bi a string separator and returns the concatenated string.if then checks if the path for the file specified is a single file or a directory here, there are two if conditions the first being checking if it is a file or not and the nested if to check whether it has to convert from JSON to Excel or vice versa.

In the elif block, once once a folder is discovered it is recursed and then converted.
