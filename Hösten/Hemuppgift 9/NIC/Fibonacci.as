//Calculates 13 first fibonacci numbers and saves them in order in RAM


	word aSize 13 // length of array a
    word a 			//This is an array
	
	// Compute start address
        loadc   r1 a        // r1 = &a[0] (the address of a[0])
		
		// Compute where to stop
        loadc   r0 a        // r0 = &a[0]
        load    r2 aSize    // r2 = number of elements in a
        add     r2 r2 r2    // r2 = 2*r2 = number of bytes in a
        add     r0 r0 r2    // r0 = &a[aLen] (first address after array)
	
		//Load start values of fibonacci 0 and 1 in register 3 and 4
		loadc r3 0			
		loadc r4 1
		//Make sure loop starts at 2, first number calculated should be a[2].
		addc    r1 2        // i++
		addc    r1 2        // i++
	
Loop:	add r5 r3 r4 		//r5=r3+r4
		storer r5 r1		//&a[i]=r5
		move r4 r3			//r3=r4
		move r5 r4			//r4=r5
		addc    r1 2        // i++
        jumpn   r1 Loop     // if &a[i] != &a[13] goto Loop