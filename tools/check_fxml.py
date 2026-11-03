"""Static check: FXML <-> controller consistency, custom components, and 'no UI code in controllers'."""
import re, sys, glob, os
import xml.etree.ElementTree as ET
R='src/main/resources/fxml/'; J='src/main/java/app/'
FX='{http://javafx.com/fxml/1}'
errors=[]; checks=0
def src(path): return open(path).read()
def has_prop(code,name):
    cap=name[0].upper()+name[1:]
    return any(re.search(p,code) for p in [r'\b(get|is)%s\s*\('%cap, r'\b%sProperty\s*\('%name, r'\bset%s\s*\('%cap])
def has_method(code,name): return re.search(r'\b%s\s*\('%name,code) is not None
def has_field(code,name): return re.search(r'@FXML\s+(private\s+)?[\w<>]+\s+([\w, ]*\b)?%s\b'%name,code) is not None
view_classes={os.path.basename(f)[:-5]:src(f) for f in glob.glob(J+'view/*.java')}
JFX_BASE={'visible','managed','text','styleClass','prefWidth','prefHeight','minWidth','minHeight','maxWidth','maxHeight','spacing','alignment','progress','style','opacity','translateX','translateY','mouseTransparent','promptText','editable','hgap','vgap','prefWrapLength','wrapText','fitToWidth','userData','onAction','tooltip','HBox.hgrow'}
for f in sorted(glob.glob(R+'*.fxml')):
    text=src(f); root=ET.fromstring(text)
    ctrl=root.get(FX+'controller'); code=None
    if ctrl: code=src(J+'controller/'+ctrl.split('.')[-1]+'.java')
    ids={e.get(FX+'id') for e in root.iter() if e.get(FX+'id')}
    for e in root.iter():
        tag=e.tag.split('}')[-1]
        for k,v in e.attrib.items():
            k=k.split('}')[-1]
            m=re.fullmatch(r'\$\{(\w+)\.(\w+)(\.\w+)*\}',v)
            if m:
                checks+=1
                owner,prop=m.group(1),m.group(2)
                if owner=='controller':
                    if not code or not has_prop(code,prop): errors.append(f'{f}: ${{controller.{prop}}} has no property in {ctrl}')
                elif owner in ids:
                    tgt=[x for x in root.iter() if x.get(FX+'id')==owner][0].tag.split('}')[-1]
                    if tgt in view_classes and not has_prop(view_classes[tgt],prop) and prop not in ('translateX','translateY'):
                        errors.append(f'{f}: ${{{owner}.{prop}}} not on {tgt}')
                else: errors.append(f'{f}: unknown owner {owner}')
            if k.startswith('on') and v.startswith('#'):
                checks+=1
                if not has_method(code,v[1:]): errors.append(f'{f}: handler {v} missing in {ctrl}')
                if tag in view_classes and k not in ('onAction',) and not re.search(r'set%s\s*\('%(k[0].upper()+k[1:]),view_classes[tag]):
                    errors.append(f'{f}: {tag} has no setter for {k}')
            if tag in view_classes and k not in JFX_BASE and not k.startswith('on') and k not in ('fx:id',) and not re.search(r'set%s\s*\('%(k[0].upper()+k[1:]),view_classes[tag]) and not re.search(r'\b%sProperty\s*\('%k, view_classes[tag]):
                # allowed if inherited from JavaFX base; only flag clearly custom-looking ones
                if k in ('seed','hex','tone','status','request','active','enabled','items','template','present','late','absent','total'):
                    errors.append(f'{f}: {tag} has no property {k}')
    if code:
        for fid in ids:
            if fid and fid!='card' and not has_field(code,fid): errors.append(f'{f}: fx:id {fid} has no @FXML field in {ctrl}')
        # @FXML fields without fx:id
        for m in re.finditer(r'@FXML\s+(?:private\s+)?[\w<>]+\s+(\w+)\s*;',code):
            checks+=1
            if m.group(1) not in ids: errors.append(f'{f}: @FXML field {m.group(1)} has no fx:id')
    # included files exist, templates exist
    for e in root.iter():
        if e.tag.endswith('include'):
            checks+=1
            if not os.path.exists(R+e.get('source')): errors.append(f'{f}: include {e.get("source")} missing')
        if e.get('template'):
            checks+=1
            if not os.path.exists('src/main/resources'+e.get('template')): errors.append(f'{f}: template {e.get("template")} missing')
# controllers: handler methods must be used, forbidden UI code
banned=[r'javafx\.scene\.layout',r'javafx\.scene\.shape',r'javafx\.scene\.paint',r'javafx\.animation',r'new (Label|Button|Region|HBox|VBox|Pane|StackPane|Tooltip|Alert|TextInputDialog|ProgressBar)\b',r'\.setStyle\(',r'getStyleClass',r'getChildren',r'setVisible|setManaged',r'setTranslate|setOpacity|Transition',r'requestFocus',r'\.setText\(|\.clear\(\)',r'Dialog|showAndWait']
for f in sorted(glob.glob(J+'controller/*.java')):
    code=src(f)
    for b in banned:
        checks+=1
        if re.search(b,code): errors.append(f'{os.path.basename(f)}: forbidden UI code matches /{b}/')
    allfx=''.join(src(x) for x in glob.glob(R+'*.fxml'))
    for m in re.finditer(r'@FXML\s+(?:private\s+)?void\s+(\w+)\s*\(',code):
        if m.group(1)=='initialize': continue
        checks+=1
        if '#'+m.group(1) not in allfx: errors.append(f'{os.path.basename(f)}: handler {m.group(1)} not used in any FXML')
print('checks:',checks,'errors:',len(errors))
for e in errors: print(' -',e)
sys.exit(1 if errors else 0)
