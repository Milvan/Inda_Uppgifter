// Writes ff, fe, ..., 2, 1, 0 to memory at address n.

        word n

        loadc   r0 -1
        loadc   r1 -1

Loop:   store   r1 n        // write r1 to memory address n
        addc    r1 -1       // r1--
        jumpn   r1 Loop     // if r1 != -1 goto Loop
