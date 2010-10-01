all: cpukern.so tensor.so gopkgs

cpukern.so: cpukern/*.cpp
	make -C cpukern

tensor.so: tensor/*.cpp
	make -C tensor

gopkgs:
	make -C go

test:   all
	make test -C cpukern
	make test -C go

.PHONY: clean
clean:
	rm -rf *.so *.a
	make clean -C cpukern
	make clean -C go