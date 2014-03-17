// Computes c = a + b

        word a 1
        word b 2
        word c

        load    r5 a        // r5 = a
        load    r6 b        // r6 = b
        add     r0 r5 r6    // r0 = r5 + r6
        store   r0 c        // c = r0
