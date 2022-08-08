
f = open("sample_user.sql", "w")
fname = open("first-names.txt", "r")
lname = open("last-names.txt", "r")
o = open("occupation.txt", "r")
    
    # User 
lname.readline()        # offset
    
occupation = o.readline()[: -1]
lastname = lname.readline()[: -1]
firstname = fname.readline()[: -1]
sin = 100000000             # 9 digit
birthdate = 20000101
password = 12345678
creditcard = 1000000000000000           # 16 digit
    
while lastname != "":
    if lastname == "Briney":
            break;
    
    email = firstname + "." + lastname + "@mail.utoronto.ca" 
    if str(birthdate)[-2 :] == "32":
        birthdate = 20000101
    
    
    query = "INSERT INTO User\nVALUES(" + str(sin) + ",'" + firstname + "','" + lastname  + "'," + str(birthdate) + ",'" + occupation + "','" + email + "','" + str(password) + "','" + str(creditcard) + "');\n"
    f.write(query)
    
    
    
    birthdate += 1
    sin += 1
    creditcard += 1
    password +=1
    occupation = o.readline()[: -1]
    lastname = lname.readline()[: -1]
    firstname = fname.readline()[: -1]
    
    
    
o.close()
lname.close()
fname.close()
f.close()
