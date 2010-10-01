# Common code for all makefiles
# Should be included in each makefile

# The C compiler
CC=gcc

# The C++ compiler
CPP=g++



# Flags to be passed to CC and CPP
CFLAGS+=\
  -Wall\
  -fPIC\
  -O3\
  -Werror\

