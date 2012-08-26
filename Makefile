NAME:=loco
PACKAGE:=org.booncode.android.loco
TARGET_ARCH:="android-8"
DOXYGEN:=doxygen
DOCU_DIR:=doc
DOXYGEN_SUBDIR:=doxygen
DOCU_IMG:=img
ANT:=ant
ANDROID:=android
ADB:=adb

-include userconfig.mk

ifdef USE_ANDROID_PATH
	PATH:="$(PATH):$(USE_ANDROID_PATH)/tools:$(USE_ANDROID_PATH)/platform-tools:"
endif


DOCU_IMG_PATH:=$(DOCU_DIR)/$(DOCU_IMG)
DOXY_DIR:=$(DOCU_DIR)/$(DOXYGEN_SUBDIR)
DOXY_IMG:=$(DOXY_DIR)/html/$(DOCU_IMG)
TARGET:=bin/$(NAME)-debug.apk

all: build


build.xml:
	$(ANDROID) update project -p . -n $(NAME) -t $(TARGET_ARCH)

$(TARGET): build.xml
	$(ANT) debug

.PHONY: clean git-clean docu info full-clean update build
.PHONY: install-dev install-emu reinstall-dev reinstall-emu uninstall-dev uninstall-emu

info:
	@echo "Commands: clean git-clean docu install-(dev/emu) reinstall-(dev/emu) uninstall-(dev/emu) full-clean update"

build: build.xml
	$(ANT) debug

git-clean: clean
	@find . -name "*~"
	find . -name "*~" -exec rm "{}" ";"
	rm -rf $(DOXY_DIR)

docu:
	mkdir -p $(DOXY_DIR)
	mkdir -p $(DOCU_IMG_PATH)
	$(DOXYGEN)
	cp -r $(DOCU_IMG_PATH) $(DOXY_IMG)

clean: build.xml
	$(ANT) clean
	rm -rf $(DOXY_DIR)

full-clean: git-clean
	rm -f local.properties proguard-project.txt project.properties build.xml

update: build.xml
	@echo "Update done"

install-dev: $(TARGET)
	$(ADB) -d install $(TARGET)

install-emu: $(TARGET)
	$(ADB) -e install $(TARGET)

reinstall-dev: $(TARGET)
	$(ADB) -d install -r $(TARGET)

reinstall-emu: $(TARGET)
	$(ADB) -e install -r $(TARGET)

uninstall-dev:
	$(ADB) -d uninstall $(PACKAGE)

uninstall-emu:
	$(ADB) -e uninstall $(PACKAGE)
