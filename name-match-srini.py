from flask import Flask, request,make_response
import json
import jellyfish
from indian_namematch import fuzzymatch
from flask_healthz import healthz,HealthError,Healthz

def soundexMatch(name1, name2, matchScore):
    soundexScore = jellyfish.jaro_winkler_similarity(jellyfish.soundex(name1), jellyfish.soundex(name2))
    match1 = fuzzymatch.single_compare(name1, name2)
    match2 = fuzzymatch.single_compare(name2, name1)

    res = []
    #Negative score condition removed
    # if (soundexScore > 0.85 or match1 == "Match" or match2 == "Match" ) and matchScore > 40:  #old
    if (soundexScore > 0.85 and matchScore > 40):
        res.append("Manual")
        res.append(soundexScore*100-5)
    return res

# Functions makeduo to make doublets of the name for comparison
# Function CompareString to compare the string with least number of doublets being formed
#=================================================================================================
def makeduo(nameList):
    duovalue=[]
    duovaluemap={}
    for i in nameList:
        if(len(i)==1):
            str="_"+i[0]
            duovalue.append(str)   
        elif(len(i)>1):
            str="_"+i[0]
            duovalue.append(str)
            str=i[-1]+"_"
            duovalue.append(str)
            for j in range(len(i)-1):
                duovalue.append(i[j:j+2])
            if(len(i)>2):
                duovalue.append(i)
    duovaluemap["length"]=len(duovalue)
    for i in duovalue:
        if i not in duovaluemap:
            duovaluemap[i]=1
        else:
            duovaluemap[i]+=1
    return duovaluemap

def CompareString(str1,str2):
    if(str1["length"]>str2["length"]):
        str1,str2=str2,str1
    #str2 having greater or equal length compared to str1
    match=0
    total=str1["length"]
    del str1["length"]
    for i in str1:
        if(i in str2):
            minval=min(str1[i],str2[i])
            match+=minval
            str2[i]-=minval
    if(total==0):
        total=1
    return (match/total)*100


#====================================================================================================

#Main function and passing values to the main function

def MainFunction(FirstString,SecondString):
    Aadhar=FirstString
    Pan=SecondString
    score=0
    match=""
    if(Aadhar=="" or Pan==""):
        score=0.0
    elif(Aadhar==None or Pan==None):
        score=0.0
    else:
        Aadhar=(Aadhar.replace("."," ")).lower().strip()
        Aadhar=(Aadhar.replace("(",""))
        Aadhar=(Aadhar.replace(")",""))
        Pan=(Pan.replace("."," ")).lower().strip()
        Aadhar=(Aadhar.replace("  "," "))
        Pan=(Pan.replace("  "," "))
        AadharArray=Aadhar.split(" ")
        PanArray=Pan.split(" ")
        tempAadharArray = list(AadharArray)
        tempPanArray = list(PanArray)
        AadharArray.sort()
        PanArray.sort()
        if(Aadhar==" " or Pan==" "):
            score=0.0
            return ("Reject",score,FirstString,SecondString)
        AadharName=Aadhar.split(" ")
        PanName=Pan.split(" ")
        AadharNameDoublets=makeduo(AadharName)
        PanNameDoublets=makeduo(PanName)

        #here we are giving the score as 100 in case of exactly same names only    
        score=CompareString(AadharNameDoublets,PanNameDoublets)
        if(Aadhar.replace(" ","")==Pan.replace(" ","")):
            score=100
        elif(score==100 and (Aadhar!=Pan and AadharArray!=PanArray)):
            score=98
                
        #names which are exactly same but have different order are assigned score 98 and taken as manual
        if score == 100:
            if tempAadharArray == tempPanArray:
                score = 100
            else:
                score=98

        #Soundex applied only for cases in range 40-75% and will be in manual or reject state only
        if score > 40 and score < 75:
            output = []
            try: 
                output = soundexMatch(Aadhar, Pan, score)
            except:
                pass
            if len(output) > 0:
                score = output[1]

    score=round(score/100, 2)

    if(score>=0.99):
        match="Accept"
    elif(score>=0.70):
        match="Manual"
    else:
        match="Reject"    

    return (match,score,FirstString,SecondString)


#====================================Endpoint Calling Approach======================================================
app = Flask(__name__)
EndpointApi= "/"
@app.route(EndpointApi, methods=['GET','POST'])

def EndpointApi():
    try:
        name1=request.args.get("name1")
    except:
        name1=None
    try:
        name2=request.args.get("name2")
    except:
        name2=None

    Match,Score,FirstName,SecondName=MainFunction(name1,name2)


    #===========================================================================================================
    #json output

    JsonVal={"match":Match,"match_score":Score,"name1":FirstName,"name2":SecondName}
    json_string = json.dumps(JsonVal)

    resp = make_response(json_string,200) 
    resp.headers['x-api-key'] = '620949b4-8990-48da-hkjasfdkkjdsb'
    return resp

#===========================================================================================================
#health and readiness probe

def liveness():
    pass

def readiness():
    try:
        MainFunction(None,None)
    except Exception:
        raise HealthError("Service is down!!!")

app.config.update(
HEALTHZ = {
    "live": "app.liveness",
    "ready": "app.readiness",
    }
)

app.register_blueprint(healthz, url_prefix="/healthz")



if __name__ == '__main__':
    app.run(debug=False ,host="0.0.0.0", port=3000)
