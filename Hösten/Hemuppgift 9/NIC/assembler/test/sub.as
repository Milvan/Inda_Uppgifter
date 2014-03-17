// Computes "sub r s[i] t[i]" and checks that r == exp[i].
// Sets r15 to 0xaa if all tests pass, 0xff otherwise.

word n 11 // number of test cases

word s   3  2  2 -3 -2 127  127    0 -128    0  127
word t   2  3 -3  2 -3 127 -127  127    1 -128 -128
word exp 1 -1  5 -5  1   0   -2 -127  127 -128   -1

        loadc   rf 0x00
        loadc   r4 exp      // r4 = &exp[0]
        loadc   r5 s        // r5 = &s[0]
        loadc   r6 t        // r6 = &t[0]

        loadc   r8 exp      // Compute address to first word after array
        load    r1 n
        add     r1 r1 r1    // r1 = 2n (two bytes per word)
        add     r8 r8 r1    // r8 = &exp[n]

Loop:   loadr   r1 r4       // r1 = r[i]
        loadr   r2 r5       // r2 = s[i]
        loadr   r3 r6       // r3 = t[i]
        sub     r0 r2 r3    // r0 = s[i]>>t[i]
        jumpn   r1 Fail     // if s[i]>>t[i] != r[i] Fail

        addc    r4 2        // i++
        addc    r5 2
        addc    r6 2
        move    r8 r0
        jumpl   r4 Loop     // if &r[i] <= &r[10] Loop

Pass:   loadc   rf 0xaa
        jump    End

Fail:   loadc   rf 0xff
        jump    End

End:
