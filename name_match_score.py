
from flask import Flask, request, make_response
import json
import jellyfish
from rapidfuzz import fuzz as fuzzymatch
from flask_health import health, HealthError

def soundexMatch(name1, name2, matchScore):
    soundexScore = jellyfish.jaro_winkler_similarity(jellyfish.soundex(name1), jellyfish.soundex(name2))
    match1 = fuzzymatch.ratio(name1, name2)
    match2 = fuzzymatch.partial_ratio(name1, name2)

    res = []

    print("soundexScore : ", soundexScore)
    print("match1 : ", match1)
    print("match2 : ", match2)
    print("matchScore : ", matchScore)

    if (soundexScore > 0.85 or match1 == "Match" or match2 == "Match") and matchScore > 40:
        res.append("Manual")

    return res

def makeDoublet(nameList):
    duovaluenap = []
    duovaluemap = {}
    for i in nameList:
        if len(i) == 1:
            str = i[0]
            duovaluenap.append(str)
            if str not in duovaluemap:
                duovaluemap[str] = 1
        elif len(i) == 2:
            str = i
            duovaluenap.append(str)
            if str not in duovaluemap:
                duovaluemap[str] = 1
        else:
            for j in range(len(i)-1):
                duovaluenap.append(i[j:j+2])
            for i in duovaluenap:
                if i not in duovaluemap:
                    duovaluemap[i] = 1
                else:
                    duovaluemap[i] += 1
    return duovaluenap

def CompareString(str1, str2):
    if len(str1) > len(str2):
        temp = str1
        str1 = str2
        str2 = temp

    match = 0
    total = len(str1)
    for i in str1:
        if i in str2:
            match += 1
            str2.remove(i)

    if total != 0:
        return (match / total) * 100
    return 0

def MainFunction(FirstString, SecondString):
    Aadhaar = FirstString
    Pan = SecondString
    score = ""
    print("Aadhaar : ", Aadhaar)
    print("Pan : ", Pan)

    if Aadhaar == "" or Pan == "":
        score = 0
    elif Aadhaar is None or Pan is None:
        score = 0
    else:
        Aadhaar = Aadhaar.replace(" ", "").lower().strip()
        Pan = Pan.replace(" ", "").lower().strip()
        AadhaarArray = Aadhaar.split()
        PanArray = Pan.split()
        AadhaarArray.sort()
        PanArray.sort()
        tempPanArray = list(AadhaarArray)
        tempPanArray.sort()

        if Aadhaar == Pan:
            print("Aadhaar == Pan")
            score = 100
        elif Aadhaar.replace(" ", "") == Pan.replace(" ", ""):
            print("Aadhaar.replace(' ', '') == Pan.replace(' ', '')")
            score = 100
        else:
            print("else")
            AadhaarNameDoublets = makeDoublet(AadhaarArray)
            PanNameDoublets = makeDoublet(PanArray)
            score = CompareString(AadhaarNameDoublets, PanNameDoublets)

            if score >= 40 and score <= 75:
                try:
                    output = soundexMatch(Aadhaar, Pan, score)
                    if len(output) > 0:
                        score = 40
                except:
                    pass

    match = ""
    if score >= 99:
        match = "Accept"
    elif score >= 70:
        match = "Manual"
    else:
        match = "Reject"

    return match, score, FirstString, SecondString

app = Flask(__name__)
EndpointApi = "/nameMatchScore"
@app.route(EndpointApi, methods=['GET', 'POST'])
def EndpointApi():
    try:
        name1 = request.args.get("name1")
    except:
        name1 = None
    try:
        name2 = request.args.get("name2")
    except:
        name2 = None

    Match, Score, FirstName, SecondName = MainFunction(name1, name2)

    jsonVal = {"match": Match, "match_score": Score, "name1": FirstName, "name2": SecondName}
    json_string = json.dumps(jsonVal)
    resp = make_response(json_string, 200)
    return resp

def liveness():
    pass

def readiness():
    try:
        MainFunction(None, None)
    except Exception:
        raise HealthError("Service is down!!!")

app.config.update(
    HEALTHZ={
        "live": "app.liveness",
        "ready": "app.readiness",
    }
)

app.register_blueprint(health, url_prefix="/healthz")

if __name__ == "__main__":
    print("Match score Ankit, Ankit : ", MainFunction("Ankit", "Ankit"))
    print("Match score Ankit Jain , Mr Ankit Jain : ", MainFunction("Ankit", "Mr Ankit Jain "))
    print("Match score Ankit J , J Ankit : ", MainFunction("Ankit J ", "J Ankit"))
    print("Match score Ankit, Sumit : ", MainFunction("Ankit", "Sumit"))
    print("Match score Ankit, Papa : ", MainFunction("Ankit", "PPPP"))
    print("Match score Ankit kumar jain , Ankit kumar jain : ", MainFunction("Ankit kumar jain", "Ankit kumar j"))
    print("Match score Jhonson , Ankit kumar jain : ", MainFunction("Johnson Wilson", "Jhonson Wilson"))
    app.run(debug=False, host="0.0.0.0", port=3000)
   

