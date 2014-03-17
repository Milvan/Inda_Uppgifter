//This program will move the word 0xff in an pattern in the RAM memory.

	word a -1 //this is the bitpattern we will be moving around (ff)
	
	load re a 		//re=a
	loadc r0 0x10   //r0= hex number 10 This is 16, and stands for that I want to loop 16/2 times , 8 times.
	
		loadc r2 0x80 		//r2= hex number 80, The starting point for the word.
Loop:	storer re r2 		//Place word a in location found in r2
		noop 				//Do nothing. Like a pause.
		storer rf r2 		//Place 00 in location found in r2 (overwrite the word a)
		addc r2 0x12 		//Increase r2 with 18. This will set the next location to be dioganally down to the right. 
		addc    r1 2        // i++
        jumpn   r1 Loop     // if r1!=r0 go to loop. (if number 16 is not reached, loop has not been done 8 times yet)

