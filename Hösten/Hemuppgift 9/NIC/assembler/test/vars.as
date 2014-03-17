code hexCodes 0x0 0x1 0xf 0x10 0x100 0x1000 0xffff
code decCodes 0 1 255 65535
word hexWords 0x0 0x1 0x7f 0x80 0xff

code hexEmpty
word wordEmpty

code hexOne 0x1
word wordOne 1

word wordNeg -1 -2 -3 -128

load r0 hexCodes
load r1 decCodes
load r2 hexWords
load r3 hexEmpty
load r4 wordEmpty

load r5 hexCodes+2
load r6 decCodes-2


