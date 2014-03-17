// Computes res = a[0] + a[1] + a[2] + ...

        word aSize 8 // length of array a
        word a -1 2 -3 4 -5 6 -7 8
        word res

        // Compute start address
        loadc   r1 a        // r1 = &a[0] (the address of a[0])

        // Compute where to stop
        loadc   r0 a        // r1 = &a[0]
        load    r2 aSize    // r2 = number of elements in a
        add     r2 r2 r2    // r2 = 2*r2 = number of bytes in a
        add     r0 r0 r2    // r0 = &a[aLen] (first address after array)

        loadc   r5 0        // r5 used to compute res
Loop:   loadr   r2 r1       // r2 = a[i]
        mul     r5 r5 r2    // r5 = r5 * r2
        addc    r1 2        // i++
        jumpn   r1 Loop     // if &a[i] != &a[8] goto Loop

        store   r5 res

