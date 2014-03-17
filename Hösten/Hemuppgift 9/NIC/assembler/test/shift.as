// Computes "rshift r s t[i]" and checks that r == exp[i].
// Sets r15 to 0xaa if all tests pass, 0xff otherwise.

word n 11 // number of test cases

word s   0xff
word t   0    1    2    7    8    9    -1   -2   -7   -8   -9
word exp 0xff 0x7f 0x3f 0x01 0x00 0x00 0xfe 0xfc 0x80 0x00 0x00

        loadc   rf 0x00     // rf indicates pass or fail
        load    r8 n        // r8 = n used as counter

        loadc   r4 exp      // r4 = &exp[0]
        load    r5 s        // r5 = s
        loadc   r6 t        // r6 = &t[0]

Loop:   loadr   r1 r4       // r1 = r[i]
        loadr   r3 r6       // r3 = t[i]
        shift   r0 r5 r3    // r0 = s>>t[i]
        jumpn   r1 Fail     // if s>>t[i] != exp[i] Fail

        addc    r4 2        // i++
        addc    r6 2
        addc    r8 -1       // n--
        loadc   r0 0        
        jumpn   r8 Loop     // if n != 0 goto Loop

Pass:   loadc   rf 0xaa
        jump    End

Fail:   loadc   rf 0xff
        jump    End

End:
