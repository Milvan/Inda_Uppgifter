# Copyright 2011 Stefan Nilsson. All rights reserved.
# Use of this source code is governed by a BSD-style
# license that can be found in the LICENSE file.
NDIR = computer
ADIR = assembler

all: nic nas

nic: $(NDIR)/Nic.java
	javac $(NDIR)/Nic.java
	jar cfe nic $(NDIR).Nic $(NDIR)/*.class
	chmod 755 nic
	rm -f $(NDIR)/*.class

nas: $(ADIR)/Nas.java
	javac $(ADIR)/Nas.java
	jar cfe nas $(ADIR).Nas $(ADIR)/*.class
	chmod 755 nas
	rm -f $(ADIR)/*.class

clean:
	rm -f $(NDIR)/*.class $(ADIR)/*.class nic nas

