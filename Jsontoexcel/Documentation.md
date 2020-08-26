**DOCUMENTATION** :

The following code has been written to convert given Json files to Excel and vice versa.

This enables users who aren&#39;t well versed with Json to access the data and make necessary changes in the Excel format. This code creates automated workflows that parses JSON and reformats it into Excel tables. It allow you to easily map the contents of a JSON dataset into their proper column and row in Excel. You are also able to extract only the portions of the JSON that you need converted to XLS and to manipulate the contents so the output Excel table fits your exact requirements.

**JavaScript Object Notation**

JavaScript Object Notation, or JSON, is an open standard data interchange format that is most commonly used to transmit data between servers and web applications as an alternative to XML.

### Excel

Microsoft Excel is considered to be the industry standard for spreadsheet applications featuring calculation and graphing tools for data analysis and reporting.

**Flow of program:**

Once the JSON , OS and pandas are imported, we define the functions.

**preprocess\_data:**

This function takes data and preprocessed data as its arguments. If then checks if the first instance is a string, integer, float ,Boolean or any value

And it then takes the pre-processed data in a dictionary form and converts it into a list.

**reverse\_preprocess\_dict:**

This function is used for reverse conversion from Excel to JSON performed in a similar manner. Here it also checks if each Excel cell has value less than 32765, which is the maximum value that an Excel cell can accommodate.

**convert\_json\_to\_excel:**

This function returns the JSON files converted to excel.

It takes the arguments old path, new path and file name. Once the file name is printed the old path is opened as &quot; f &quot; and the data is loaded.

The tenant ID and module name are stored and once the filename is extracted, the tenant ID and module name are popped because we want to access only column within.

The tenant ID and module name are popped.

If length of data keys is equal to 1 the conversion starts. Here we are checking if if the file has more than one master. If it does a message is printed. In the conversion block the data is converted from dictionaries to data frame and the file is renamed with a new path\_tenantId\_ module name\_ file name with xlsx extension.

The file is saved.

**convert\_excel\_to\_json:**

This function takes old path, new path and filename as its arguments.

It then prints file name. Tenant ID, module name and master are extracted. The Excel is read and the JSON is loaded, at this point we add the tenant ID and module name to the head so as to prevent loss of data and to get back the original JSON.

**convert:**

This function takes the source, destination and JSON to excel as arguments. The json\_ to\_excel argument returns a true if the conversion is to take place from JSON to excel and false if it has to be converted from Excel to JSON. The if condition executes after this is checked.

It checks if the the path doesn&#39;t exist and makes directories at the destination.

It joins the source with the item. It uses the join function. The join () method provides a flexible way to create string from iterable objects. Is joins each element of an iterable such as list, string and tuple bi a string separator and returns the concatenated string.if then checks if the path for the file specified is a single file or a directory here, there are two if conditions the first being checking if it is a file or not and the nested if to check whether it has to convert from JSON to Excel or vice versa.

In the elif block, once once a folder is discovered it is recursed and then converted.