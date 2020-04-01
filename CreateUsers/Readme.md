# Create Users

This program uses the data from file **data.csv**. Please fill in the data in the data.csv, you can export the excel data to csv, before running script.

## Install Nodejs

Download and install from [nodejs](https://nodejs.org/en/#home-downloadhead)

## Download Code

Download the create user from the [github](https://github.com/egovernments/ukd-rainmaker-customization) or

If git is installed use below command to get code

```
git clone http://github.com
```

## Running Script

Open terminal and navigate to the to the downloaded folder

For first time please run
```
npm install 
```

```
node index
```

If and failed records are there I will print in console please take note of them and retry with correct values

## CSV format

Format

```
tenantId
name
mobileNumber
correspondenceAddress
fatherOrHusbandName
dob
gender
roles
assignmentFromDate
department
designation
boundaryType
boundary
employeeType
employeeStatus
```

Example

```
uk.haridwar,Deepu,9999999989,Haridwar,Sunil,26/03/2000,MALE,EMPLOYEE|TL_APPROVER,26/03/2019,ADM,RO01,City,uk.haridwar,PERMANENT,EMPLOYED
uk.haridwar,Susanth,9999999989,Haridwar,Sunil,26/03/2000,MALE,EMPLOYEE|TL_APPROVER,26/03/2019,ADM,RO01,City,uk.haridwar,PERMANENT,EMPLOYED
```

## Allowed Values

employement type

```
TEMPORARY
PERMANENT
DAILYWAGES
CONTRACT
DEPUTATION
```

boundaryType

```
CITY
ZONE
LOCALITY
BLOCK
```

roles ( | delimiter seperated)

```
CITIZEN
EMPLOYEE
SUPERUSER
GRO
TL_CEMP
TL_APPROVER
DGRO
CSR
PGR-ADMIN
CEMP
FEMP
STADMIN
EGF_BILL_CREATOR
EGF_BILL_APPROVER
EGF_VOUCHER_CREATOR
EGF_VOUCHER_APPROVER
EGF_PAYMENT_CREATOR
EGF_PAYMENT_APPROVER
EGF_MASTER_ADMIN
EGF_REPORT_VIEW
COLL_RECEIPT_CREATOR
COLL_REMIT_TO_BANK
SYS_INTEGRATOR_FINANCE
SYS_INTEGRATOR_WATER_SEW
EMPLOYEE_FINANCE
EGF_ADMINISTRATOR
TL_FIELD_INSPECTOR
TL_DOC_VERIFIER
PUNJAB_EODB
PTULBADMIN
PTSTADMIN
PTCEMP
ULBADMIN
PTFEMP
LOCALIZATION_UPLOADER
HRMS_ADMIN
GIS_USER
DATAENTRY_ADMIN
UC_EMP
```

gender

```
MALE
FEMALE
OTHER
```

Department

```
ADM
REV
ACN
PHS
EE
```

Designation

```
MC01
AMC01
DMC01
ASTMC01
RO01
TS01
TI01
TC01
FO01
AAO01
ACNT01
AA01
MO01
ZSI01
CSI01
SI01
SK01
SE01
EE01
AE01
JE01
```
