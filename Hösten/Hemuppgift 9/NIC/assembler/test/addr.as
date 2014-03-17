word w0
word w1
code c0
code c1

        jump    c0
Start:  jumpe   r0 c1
        jumpn   r0 Start

        load    r0 w0
        store   r0 w1
        store   r0 End

        loadc   r0 w0
        addc    r0 w1
        addc    r0 Start

        jump    c0-20
        jumpe   r0 16
        jumpn   r0 Start+8
        jumple  r0 End-8

        load    r0 w0+22
        store   r0 w1-22
        load    r0 Start+22
        store   r0 End-22

        loadc   r0 w0+17
        addc    r0 w1-17
        loadc   r0 Start+13
End:    addc    r0 End-13
