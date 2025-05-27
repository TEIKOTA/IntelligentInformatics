JAVAC=javac
SRC_DIRS=ap25unit1/ap25 ap25unit1/myplayer
BIN_DIR=ap25unit1/bin

SOURCES=$(foreach dir,$(SRC_DIRS),$(wildcard $(dir)/*.java))

test: all
	@for classfile in $(CLASSES); do \
        classname=$$(echo $$classfile | sed 's|$(BIN_DIR)/||;s|/|.|g;s|\.class$$||'); \
        java -cp $(BIN_DIR) $$classname; \
	done

.PHONY: all clean

all:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SOURCES)

clean:
	rm -rf $(BIN_DIR)