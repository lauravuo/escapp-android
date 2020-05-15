#
print "Start"
file = open('data.txt', 'r')
content = file.read();
file.close()

lines = content.split('\n')
for line in lines:
    words = line.split()
    semifinalNbr = words[0]
    country = words[1]
    language = words[2]
    i = 3
    artist = ""
    while i < (len(words) - 2):
        artist = artist + words[i] + " "
        i = i + 1
    words = artist.split('"')
    print "    <entry>"
    print "        <country>" + country + "</country>"
    print "        <artist>" + words[0] + "</artist>"
    print "        <title>" + words[1] + "</title>"
    print "        <englishTitle>" + words[2] + "</englishTitle>"
    print "        <language>" + language + "</language>"
    print "        <semifinal>3</semifinal>"
    print "        <finalNbr>" + semifinalNbr + "</finalNbr>"
    print "    </entry>"
