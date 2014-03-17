word w0
word w1
code c0
code c1

Start:  jump    2
        jumpe   r0 c0-2
        jumple  r0 3
        jumpn   r0 Stop+2
        load    r0 w0+1
        store   r0 w3-1
        store   r0 End+1
        loadc   r0 w0-250
        addc    r0 c0+25000
        jump    c1-1001
        jump    c2+800
End:    addc    r0 Start-100
