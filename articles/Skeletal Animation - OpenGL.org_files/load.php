function isCompatible(){if(navigator.appVersion.indexOf('MSIE')!==-1&&parseFloat(navigator.appVersion.split('MSIE')[1])<6){return false;}return true;}var startUp=function(){mw.config=new mw.Map(true);mw.loader.addSource({"local":{"loadScript":"/wiki_132/load.php","apiScript":"/wiki_132/api.php"}});mw.loader.register([["site","1364481819",[],"site"],["noscript","1364481819",[],"noscript"],["startup","1364763489",[],"startup"],["filepage","1364481819"],["user.groups","1364481819",[],"user"],["user","1364481819",[],"user"],["user.cssprefs","1364763489",["mediawiki.user"],"private"],["user.options","1364763489",[],"private"],["user.tokens","1364481819",[],"private"],["mediawiki.language.data","1364481819",["mediawiki.language.init"]],["skins.chick","1364481819"],["skins.cologneblue","1364481819"],["skins.modern","1364481819"],["skins.monobook","1364481819"],["skins.nostalgia","1364481819"],["skins.simple","1364481819"],["skins.standard","1364481819"],["skins.vector","1364481819"],["jquery"
,"1364481819"],["jquery.appear","1364481819"],["jquery.arrowSteps","1364481819"],["jquery.async","1364481819"],["jquery.autoEllipsis","1364481819",["jquery.highlightText"]],["jquery.badge","1364481819"],["jquery.byteLength","1364481819"],["jquery.byteLimit","1364481819",["jquery.byteLength"]],["jquery.checkboxShiftClick","1364481819"],["jquery.client","1364481819"],["jquery.collapsibleTabs","1364481819"],["jquery.color","1364481819",["jquery.colorUtil"]],["jquery.colorUtil","1364481819"],["jquery.cookie","1364481819"],["jquery.delayedBind","1364481819"],["jquery.expandableField","1364481819",["jquery.delayedBind"]],["jquery.farbtastic","1364481819",["jquery.colorUtil"]],["jquery.footHovzer","1364481819"],["jquery.form","1364481819"],["jquery.getAttrs","1364481819"],["jquery.highlightText","1364481819",["jquery.mwExtension"]],["jquery.hoverIntent","1364481819"],["jquery.json","1364481819"],["jquery.localize","1364481819"],["jquery.makeCollapsible","1364481823"],["jquery.mockjax",
"1364481819"],["jquery.mw-jump","1364481819"],["jquery.mwExtension","1364481819"],["jquery.placeholder","1364481819"],["jquery.qunit","1364481819"],["jquery.qunit.completenessTest","1364481819",["jquery.qunit"]],["jquery.spinner","1364481819"],["jquery.jStorage","1364481819",["jquery.json"]],["jquery.suggestions","1364481819",["jquery.autoEllipsis"]],["jquery.tabIndex","1364481819"],["jquery.tablesorter","1364481819",["jquery.mwExtension"]],["jquery.textSelection","1364481819",["jquery.client"]],["jquery.validate","1364481819"],["jquery.xmldom","1364481819"],["jquery.tipsy","1364481819"],["jquery.ui.core","1364481819",["jquery"],"jquery.ui"],["jquery.ui.widget","1364481819",[],"jquery.ui"],["jquery.ui.mouse","1364481819",["jquery.ui.widget"],"jquery.ui"],["jquery.ui.position","1364481819",[],"jquery.ui"],["jquery.ui.draggable","1364481819",["jquery.ui.core","jquery.ui.mouse","jquery.ui.widget"],"jquery.ui"],["jquery.ui.droppable","1364481819",["jquery.ui.core","jquery.ui.mouse",
"jquery.ui.widget","jquery.ui.draggable"],"jquery.ui"],["jquery.ui.resizable","1364481819",["jquery.ui.core","jquery.ui.widget","jquery.ui.mouse"],"jquery.ui"],["jquery.ui.selectable","1364481819",["jquery.ui.core","jquery.ui.widget","jquery.ui.mouse"],"jquery.ui"],["jquery.ui.sortable","1364481819",["jquery.ui.core","jquery.ui.widget","jquery.ui.mouse"],"jquery.ui"],["jquery.ui.accordion","1364481819",["jquery.ui.core","jquery.ui.widget"],"jquery.ui"],["jquery.ui.autocomplete","1364481819",["jquery.ui.core","jquery.ui.widget","jquery.ui.position"],"jquery.ui"],["jquery.ui.button","1364481819",["jquery.ui.core","jquery.ui.widget"],"jquery.ui"],["jquery.ui.datepicker","1364481819",["jquery.ui.core"],"jquery.ui"],["jquery.ui.dialog","1364481819",["jquery.ui.core","jquery.ui.widget","jquery.ui.button","jquery.ui.draggable","jquery.ui.mouse","jquery.ui.position","jquery.ui.resizable"],"jquery.ui"],["jquery.ui.progressbar","1364481819",["jquery.ui.core","jquery.ui.widget"],"jquery.ui"],[
"jquery.ui.slider","1364481819",["jquery.ui.core","jquery.ui.widget","jquery.ui.mouse"],"jquery.ui"],["jquery.ui.tabs","1364481819",["jquery.ui.core","jquery.ui.widget"],"jquery.ui"],["jquery.effects.core","1364481819",["jquery"],"jquery.ui"],["jquery.effects.blind","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.bounce","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.clip","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.drop","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.explode","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.fade","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.fold","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.highlight","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.pulsate","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.scale","1364481819",["jquery.effects.core"],"jquery.ui"],[
"jquery.effects.shake","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.slide","1364481819",["jquery.effects.core"],"jquery.ui"],["jquery.effects.transfer","1364481819",["jquery.effects.core"],"jquery.ui"],["mediawiki","1364481819"],["mediawiki.api","1364481819",["mediawiki.util"]],["mediawiki.api.category","1364481819",["mediawiki.api","mediawiki.Title"]],["mediawiki.api.edit","1364481819",["mediawiki.api","mediawiki.Title"]],["mediawiki.api.parse","1364481819",["mediawiki.api"]],["mediawiki.api.watch","1364481819",["mediawiki.api","user.tokens"]],["mediawiki.debug","1364481819",["jquery.footHovzer"]],["mediawiki.debug.init","1364481819",["mediawiki.debug"]],["mediawiki.feedback","1364481819",["mediawiki.api.edit","mediawiki.Title","mediawiki.jqueryMsg","jquery.ui.dialog"]],["mediawiki.htmlform","1364481819"],["mediawiki.notification","1364481819",["mediawiki.page.startup"]],["mediawiki.notify","1364481819"],["mediawiki.searchSuggest","1364481823",[
"jquery.autoEllipsis","jquery.client","jquery.placeholder","jquery.suggestions"]],["mediawiki.Title","1364481819",["mediawiki.util"]],["mediawiki.Uri","1364481819"],["mediawiki.user","1364481819",["jquery.cookie","mediawiki.api"]],["mediawiki.util","1364481822",["jquery.client","jquery.cookie","jquery.mwExtension","mediawiki.notify"]],["mediawiki.action.edit","1364481819",["jquery.textSelection","jquery.byteLimit"]],["mediawiki.action.edit.preview","1364481819",["jquery.form","jquery.spinner"]],["mediawiki.action.history","1364481819",[],"mediawiki.action.history"],["mediawiki.action.history.diff","1364481819",[],"mediawiki.action.history"],["mediawiki.action.view.dblClickEdit","1364481819",["mediawiki.util"]],["mediawiki.action.view.metadata","1364481819"],["mediawiki.action.view.rightClickEdit","1364481819"],["mediawiki.action.watch.ajax","1364481819",["mediawiki.page.watch.ajax"]],["mediawiki.language","1364481819",["mediawiki.language.data","mediawiki.cldr"]],["mediawiki.cldr",
"1364481819",["mediawiki.libs.pluralruleparser"]],["mediawiki.libs.pluralruleparser","1364481819"],["mediawiki.language.init","1364481819"],["mediawiki.jqueryMsg","1364481819",["mediawiki.util","mediawiki.language"]],["mediawiki.libs.jpegmeta","1364481819"],["mediawiki.page.ready","1364481819",["jquery.checkboxShiftClick","jquery.makeCollapsible","jquery.placeholder","jquery.mw-jump","mediawiki.util"]],["mediawiki.page.startup","1364481819",["jquery.client","mediawiki.util"]],["mediawiki.page.watch.ajax","1364513247",["mediawiki.page.startup","mediawiki.api.watch","mediawiki.util","mediawiki.notify","jquery.mwExtension"]],["mediawiki.special","1364481819"],["mediawiki.special.block","1364481819",["mediawiki.util"]],["mediawiki.special.changeemail","1364481819",["mediawiki.util"]],["mediawiki.special.changeslist","1364481819",["jquery.makeCollapsible"]],["mediawiki.special.movePage","1364481819",["jquery.byteLimit"]],["mediawiki.special.preferences","1364481819"],[
"mediawiki.special.recentchanges","1364481819",["mediawiki.special"]],["mediawiki.special.search","1364482856"],["mediawiki.special.undelete","1364481819"],["mediawiki.special.upload","1364481819",["mediawiki.libs.jpegmeta","mediawiki.util"]],["mediawiki.special.javaScriptTest","1364481819",["jquery.qunit"]],["mediawiki.tests.qunit.testrunner","1364481819",["jquery.qunit","jquery.qunit.completenessTest","mediawiki.page.startup","mediawiki.page.ready"]],["mediawiki.legacy.ajax","1364481819",["mediawiki.util","mediawiki.legacy.wikibits"]],["mediawiki.legacy.commonPrint","1364481819"],["mediawiki.legacy.config","1364481819",["mediawiki.legacy.wikibits"]],["mediawiki.legacy.IEFixes","1364481819",["mediawiki.legacy.wikibits"]],["mediawiki.legacy.protect","1364481819",["mediawiki.legacy.wikibits","jquery.byteLimit"]],["mediawiki.legacy.shared","1364481819"],["mediawiki.legacy.oldshared","1364481819"],["mediawiki.legacy.upload","1364481819",["mediawiki.legacy.wikibits","mediawiki.util"]],[
"mediawiki.legacy.wikibits","1364481819",["mediawiki.util"]],["mediawiki.legacy.wikiprintable","1364481819"],["ext.articleFeedback.startup","1364481819",["mediawiki.util","mediawiki.user"]],["ext.articleFeedback","1364481823",["jquery.ui.dialog","jquery.ui.button","jquery.articleFeedback","jquery.cookie","jquery.clickTracking","ext.articleFeedback.ratingi18n"]],["ext.articleFeedback.ratingi18n","1364481819"],["ext.articleFeedback.dashboard","1364481819"],["jquery.articleFeedback","1364481823",["jquery.appear","jquery.tipsy","jquery.json","jquery.localize","jquery.ui.dialog","jquery.ui.button","jquery.cookie","jquery.clickTracking","mediawiki.jqueryMsg","mediawiki.language"]],["ext.math.mathjax","1364481819",[],"ext.math.mathjax"],["ext.math.mathjax.enabler","1364481819"],["mediawiki.api.titleblacklist","1364481819",["mediawiki.api"]],["ext.geshi.local","1364481819"],["ext.categoryTree","1364484002"],["ext.categoryTree.css","1364481819"],["ext.abuseFilter","1364481819"],[
"ext.abuseFilter.edit","1364522999",["mediawiki.util","mediawiki.api","jquery.textSelection","jquery.spinner"]],["ext.abuseFilter.tools","1364481819",["mediawiki.api","mediawiki.notify","user.tokens","jquery.spinner"]],["ext.abuseFilter.examine","1364481819",["jquery.spinner","mediawiki.api"]],["ext.vector.collapsibleNav","1364481823",["mediawiki.util","jquery.client","jquery.cookie","jquery.tabIndex"],"ext.vector"],["ext.vector.collapsibleTabs","1364481819",["jquery.collapsibleTabs","jquery.delayedBind"],"ext.vector"],["ext.vector.editWarning","1364481823",[],"ext.vector"],["ext.vector.expandableSearch","1364481819",["jquery.client","jquery.expandableField","jquery.delayedBind"],"ext.vector"],["ext.vector.footerCleanup","1364481819",["mediawiki.jqueryMsg","jquery.cookie"],"ext.vector"],["ext.vector.sectionEditLinks","1364481819",["jquery.cookie","jquery.clickTracking"],"ext.vector"],["contentCollector","1364481819",[],"ext.wikiEditor"],["jquery.wikiEditor","1364513264",[
"jquery.client","jquery.textSelection","jquery.delayedBind"],"ext.wikiEditor"],["jquery.wikiEditor.iframe","1364481819",["jquery.wikiEditor","contentCollector"],"ext.wikiEditor"],["jquery.wikiEditor.dialogs","1364481819",["jquery.wikiEditor","jquery.wikiEditor.toolbar","jquery.ui.dialog","jquery.ui.button","jquery.ui.draggable","jquery.ui.resizable","jquery.tabIndex"],"ext.wikiEditor"],["jquery.wikiEditor.dialogs.config","1364513264",["jquery.wikiEditor","jquery.wikiEditor.dialogs","jquery.wikiEditor.toolbar.i18n","jquery.suggestions","mediawiki.Title"],"ext.wikiEditor"],["jquery.wikiEditor.highlight","1364481819",["jquery.wikiEditor","jquery.wikiEditor.iframe"],"ext.wikiEditor"],["jquery.wikiEditor.preview","1364481819",["jquery.wikiEditor"],"ext.wikiEditor"],["jquery.wikiEditor.previewDialog","1364481819",["jquery.wikiEditor","jquery.wikiEditor.dialogs"],"ext.wikiEditor"],["jquery.wikiEditor.publish","1364481819",["jquery.wikiEditor","jquery.wikiEditor.dialogs"],"ext.wikiEditor"],[
"jquery.wikiEditor.templateEditor","1364481819",["jquery.wikiEditor","jquery.wikiEditor.iframe","jquery.wikiEditor.dialogs"],"ext.wikiEditor"],["jquery.wikiEditor.templates","1364481819",["jquery.wikiEditor","jquery.wikiEditor.iframe"],"ext.wikiEditor"],["jquery.wikiEditor.toc","1364481819",["jquery.wikiEditor","jquery.wikiEditor.iframe","jquery.ui.draggable","jquery.ui.resizable","jquery.autoEllipsis","jquery.color"],"ext.wikiEditor"],["jquery.wikiEditor.toolbar","1364481819",["jquery.wikiEditor","jquery.wikiEditor.toolbar.i18n"],"ext.wikiEditor"],["jquery.wikiEditor.toolbar.config","1364481819",["jquery.wikiEditor","jquery.wikiEditor.toolbar.i18n","jquery.wikiEditor.toolbar","jquery.cookie","jquery.async"],"ext.wikiEditor"],["jquery.wikiEditor.toolbar.i18n","1364481819",[],"ext.wikiEditor"],["ext.wikiEditor","1364481819",["jquery.wikiEditor"],"ext.wikiEditor"],["ext.wikiEditor.dialogs","1364481819",["ext.wikiEditor","ext.wikiEditor.toolbar","jquery.wikiEditor.dialogs",
"jquery.wikiEditor.dialogs.config"],"ext.wikiEditor"],["ext.wikiEditor.highlight","1364481819",["ext.wikiEditor","jquery.wikiEditor.highlight"],"ext.wikiEditor"],["ext.wikiEditor.preview","1364513264",["ext.wikiEditor","jquery.wikiEditor.preview"],"ext.wikiEditor"],["ext.wikiEditor.previewDialog","1364481819",["ext.wikiEditor","jquery.wikiEditor.previewDialog"],"ext.wikiEditor"],["ext.wikiEditor.publish","1364481819",["ext.wikiEditor","jquery.wikiEditor.publish"],"ext.wikiEditor"],["ext.wikiEditor.templateEditor","1364481819",["ext.wikiEditor","ext.wikiEditor.highlight","jquery.wikiEditor.templateEditor"],"ext.wikiEditor"],["ext.wikiEditor.templates","1364481819",["ext.wikiEditor","ext.wikiEditor.highlight","jquery.wikiEditor.templates"],"ext.wikiEditor"],["ext.wikiEditor.toc","1364481819",["ext.wikiEditor","ext.wikiEditor.highlight","jquery.wikiEditor.toc"],"ext.wikiEditor"],["ext.wikiEditor.tests.toolbar","1364481819",["ext.wikiEditor.toolbar"],"ext.wikiEditor"],[
"ext.wikiEditor.toolbar","1364481819",["ext.wikiEditor","jquery.wikiEditor.toolbar","jquery.wikiEditor.toolbar.config"],"ext.wikiEditor"],["ext.wikiEditor.toolbar.hideSig","1364481819",[],"ext.wikiEditor"],["ext.checkUser","1364481819",["mediawiki.util"]],["ext.nuke","1364481819"],["jquery.clickTracking","1364481819",["jquery.cookie","mediawiki.util"]],["ext.clickTrackingSidebar","1364481819",["jquery.clickTracking"]],["ext.UserBuckets","1364481819",["jquery.clickTracking","jquery.json","jquery.cookie"]]]);mw.config.set({"wgLoadScript":"/wiki_132/load.php","debug":false,"skin":"vector","stylepath":"/wiki_132/skins","wgUrlProtocols":"http\\:\\/\\/|https\\:\\/\\/|ftp\\:\\/\\/|irc\\:\\/\\/|ircs\\:\\/\\/|gopher\\:\\/\\/|telnet\\:\\/\\/|nntp\\:\\/\\/|worldwind\\:\\/\\/|mailto\\:|news\\:|svn\\:\\/\\/|git\\:\\/\\/|mms\\:\\/\\/|\\/\\/","wgArticlePath":"/wiki/$1","wgScriptPath":"/wiki_132","wgScriptExtension":".php","wgScript":"/wiki_132/index.php","wgVariantArticlePath":false,"wgActionPaths":{
},"wgServer":"http://www.opengl.org","wgUserLanguage":"en","wgContentLanguage":"en","wgVersion":"1.20.2","wgEnableAPI":true,"wgEnableWriteAPI":true,"wgMainPageTitle":"Main Page","wgFormattedNamespaces":{"-2":"Media","-1":"Special","0":"","1":"Talk","2":"User","3":"User talk","4":"OpenGL.org","5":"OpenGL.org talk","6":"File","7":"File talk","8":"MediaWiki","9":"MediaWiki talk","10":"Template","11":"Template talk","12":"Help","13":"Help talk","14":"Category","15":"Category talk"},"wgNamespaceIds":{"media":-2,"special":-1,"":0,"talk":1,"user":2,"user_talk":3,"opengl.org":4,"opengl.org_talk":5,"file":6,"file_talk":7,"mediawiki":8,"mediawiki_talk":9,"template":10,"template_talk":11,"help":12,"help_talk":13,"category":14,"category_talk":15,"image":6,"image_talk":7,"project":4,"project_talk":5},"wgSiteName":"OpenGL.org","wgFileExtensions":["png","gif","jpg","jpeg"],"wgDBname":"opengl_org_wiki","wgFileCanRotate":true,"wgAvailableSkins":{"cologneblue":"CologneBlue","simple":"Simple","modern":
"Modern","nostalgia":"Nostalgia","myskin":"MySkin","vector":"Vector","chick":"Chick","monobook":"MonoBook","standard":"Standard"},"wgExtensionAssetsPath":"/wiki_132/extensions","wgCookiePrefix":"opengl_org_wiki_mw_","wgResourceLoaderMaxQueryLength":-1,"wgCaseSensitiveNamespaces":[],"wgCollapsibleNavBucketTest":false,"wgCollapsibleNavForceNewVersion":false,"wgWikiEditorToolbarClickTracking":false,"wgWikiEditorMagicWords":{"redirect":"#REDIRECT","img_right":"right","img_left":"left","img_none":"none","img_center":"center","img_thumbnail":"thumbnail","img_framed":"framed","img_frameless":"frameless"},"wgArticleFeedbackSMaxage":2592000,"wgArticleFeedbackCategories":[],"wgArticleFeedbackBlacklistCategories":["Main Page"],"wgArticleFeedbackLotteryOdds":100,"wgArticleFeedbackTracking":{"buckets":{"ignore":100,"track":0},"version":0,"expires":30},"wgArticleFeedbackOptions":{"buckets":{"show":100,"hide":0},"version":0,"expires":30},"wgArticleFeedbackNamespaces":[0],
"wgArticleFeedbackWhatsThisPage":"Project:ArticleFeedback","wgArticleFeedbackRatingTypesFlipped":{"trustworthy":1,"objective":2,"complete":3,"wellwritten":4}});};if(isCompatible()){document.write("\x3cscript src=\"/wiki_132/load.php?debug=false\x26amp;lang=en\x26amp;modules=jquery%2Cmediawiki\x26amp;only=scripts\x26amp;skin=vector\x26amp;version=20121204T210731Z\"\x3e\x3c/script\x3e");}delete isCompatible;
/* cache key: opengl_org_wiki-mw_:resourceloader:filter:minify-js:7:124ff5c6168d4e2f4062e3e4d9fa0307 */