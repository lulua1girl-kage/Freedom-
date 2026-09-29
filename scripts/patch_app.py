from pathlib import Path
import sys
p=Path(sys.argv[1]);s=p.read_text(encoding='utf-8')
marker="function sessionNowIso(){return new Date(Date.now()).toISOString()}"
bridge=r'''function nativePackagesForApps(apps){const map={'instagram':'com.instagram.android','pinterest':'com.pinterest','youtube':'com.google.android.youtube','tiktok':'com.zhiliaoapp.musically','facebook':'com.facebook.katana','twitter':'com.twitter.android','x':'com.twitter.android','chrome':'com.android.chrome','reddit':'com.reddit.frontpage','snapchat':'com.snapchat.android','discord':'com.discord','telegram':'org.telegram.messenger','netflix':'com.netflix.mediaclient','spotify':'com.spotify.music','threads':'com.instagram.barcelona'};return [...new Set((apps||[]).map(x=>{const v=String(x).trim();return v.includes('.')?v:map[v.toLowerCase()];}).filter(Boolean))];}
function nativeAppsForBlocklist(name){const list=state.blocklists.find(x=>x.name===name);const apps=(name==='All distracting apps'||!list)?state.blockedApps.map(x=>x.name):(list.apps||[]);return nativePackagesForApps(apps);}
function startNativeProtection(session){if(!window.KageNative||!session)return true;const packages=nativeAppsForBlocklist(session.blocklist);if(!packages.length){toast('No supported apps are selected for native blocking. Add a supported app to the blocklist.');return false;}const ok=window.KageNative.startProtection(packages.join(','),new Date(session.endAt).getTime());if(!ok){window.KageNative.openAccessibilitySettings();toast('Enable KAGE Focus accessibility access, then start the session again.');return false;}return true;}
function stopNativeProtection(){if(window.KageNative)window.KageNative.stopProtection();}'''
if marker not in s: raise SystemExit('session marker not found')
s=s.replace(marker,bridge+'\n'+marker,1)
old="state.sessions.unshift(session);state.focusEnabled=true;applyBlocklist(session.blocklist,true);save();toast(`Protection active until ${formatEndDate(session.endAt)}.`);route='home';render();"
new="if(!startNativeProtection(session))return;state.sessions.unshift(session);state.focusEnabled=true;applyBlocklist(session.blocklist,true);save();toast(`Protection active until ${formatEndDate(session.endAt)}.`);route='home';render();"
if old not in s: raise SystemExit('start session code not found')
s=s.replace(old,new,1)
old2="state.focusMinutes+=earned;state.focusEnabled=false;applyBlocklist(s.blocklist,false);save();toast(auto?'Focus session finished exactly on schedule.':'Session ended. Focus time saved.');render();"
new2="state.focusMinutes+=earned;state.focusEnabled=false;applyBlocklist(s.blocklist,false);stopNativeProtection();save();toast(auto?'Focus session finished exactly on schedule.':'Session ended. Focus time saved.');render();"
if old2 not in s: raise SystemExit('end session code not found')
s=s.replace(old2,new2,1)
old3="active.status='ACTIVE';active.lastHydratedAt=sessionNowIso();state.focusEnabled=true;applyBlocklist(active.blocklist,true);save();"
new3="active.status='ACTIVE';active.lastHydratedAt=sessionNowIso();state.focusEnabled=true;applyBlocklist(active.blocklist,true);if(!startNativeProtection(active)){state.focusEnabled=false;active.active=false;active.status='BLOCKER_REQUIRED';save();return;}save();"
if old3 not in s: raise SystemExit('hydrate code not found')
s=s.replace(old3,new3,1)
p.write_text(s,encoding='utf-8')
