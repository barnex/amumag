for i in amu/*/*.java amu/*/*/*.java x/*.java refsh/*.java; do cat header.txt $i > temp; mv temp $i; done

