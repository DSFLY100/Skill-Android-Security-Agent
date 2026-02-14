package eff;

public final class C35035z0 {
    public static void m78045b() {
        if (!com.kwai.robust.PatchProxy.applyVoid(null, eff.C35035z0.class, "18") && boi.C5439j1.m12660h()) {
            java.lang.RuntimeException runtimeException = new java.lang.RuntimeException("IM-Thread-Check, 禁止主线程调用");
            r87.C69821c.m129835d("CONV|", "禁止主线程调用", runtimeException);
            if (ib8.C45178a.m92730e()) {
                throw runtimeException;
            }
        }
    }

    public static void m78044a() {
        if (!com.kwai.robust.PatchProxy.applyVoid(null, eff.C35035z0.class, "17") && !boi.C5439j1.m12660h()) {
            java.lang.RuntimeException runtimeException = new java.lang.RuntimeException("IM-Thread-Check, 禁止子程调用");
            r87.C69821c.m129834c("CONV|", "禁止子线程调用, cur thread is " + java.lang.Thread.currentThread().getName());
            if (ib8.C45178a.m92730e()) {
                throw runtimeException;
            }
        }
    }

    public static void m78055l(@p698w0.InterfaceC82235a final com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        if (com.kwai.robust.PatchProxy.applyVoidOneRefs(kwaiMsg, null, eff.C35035z0.class, "14")) {
            return;
        }
        yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                kwaiMsg.setExtra(com.google.protobuf.nano.MessageNano.toByteArray((ej6.InterfaceC35214c.t0) obj));
                return null;
            }
        });
    }

    public static void m78060q(@p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        if (com.kwai.robust.PatchProxy.applyVoidOneRefs(kwaiMsg, null, eff.C35035z0.class, com.kuaishou.android.model.ads.PhotoAdvertisement.COMMENT_ACTIONBAR_STYLE_13)) {
            return;
        }
        yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                ((ej6.InterfaceC35214c.t0) obj).f122143j = true;
                return null;
            }
        });
    }

    public static boolean m78049f(com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        java.lang.Object objApplyOneRefs = com.kwai.robust.PatchProxy.applyOneRefs(kwaiMsg, null, eff.C35035z0.class, "4");
        if (objApplyOneRefs != com.kwai.robust.PatchProxyResult.class) {
            return ((java.lang.Boolean) objApplyOneRefs).booleanValue();
        }
        int msgType = kwaiMsg.getMsgType();
        if (msgType < 200 && msgType >= 100) {
            return false;
        }
        return true;
    }

    public static boolean m78050g(java.lang.String str) {
        java.lang.Object objApplyOneRefs = com.kwai.robust.PatchProxy.applyOneRefs(str, null, eff.C35035z0.class, "3");
        if (objApplyOneRefs != com.kwai.robust.PatchProxyResult.class) {
            return ((java.lang.Boolean) objApplyOneRefs).booleanValue();
        }
        java.lang.Object objApplyOneRefs2 = com.kwai.robust.PatchProxy.applyOneRefs(str, null, a7h.C0459q.class, "5");
        if (objApplyOneRefs2 != com.kwai.robust.PatchProxyResult.class) {
            return ((java.lang.Boolean) objApplyOneRefs2).booleanValue();
        }
        if (com.yxcorp.utility.TextUtils.m71737z(str) || "90041".equals(str) || (str.startsWith("403") && str.length() == 8)) {
            return false;
        }
        return true;
    }

    public static boolean m78051h(java.lang.String str) {
        java.lang.Object objApplyOneRefs = com.kwai.robust.PatchProxy.applyOneRefs(str, null, eff.C35035z0.class, com.kuaishou.android.model.ads.PhotoAdvertisement.COMMENT_ACTIONBAR_STYLE_2);
        if (objApplyOneRefs != com.kwai.robust.PatchProxyResult.class) {
            return ((java.lang.Boolean) objApplyOneRefs).booleanValue();
        }
        if (com.yxcorp.utility.TextUtils.m71737z(str)) {
            return false;
        }
        if ("90041".equals(str)) {
            return true;
        }
        if (!str.startsWith("403") || str.length() != 8) {
            return false;
        }
        return true;
    }

    public static void m78054k(@p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        if (com.kwai.robust.PatchProxy.applyVoidOneRefs(kwaiMsg, null, eff.C35035z0.class, "8")) {
            return;
        }
        boolean zM157654b = z09.C90402k.m157654b();
        boolean zM157656d = z09.C90402k.m157656d();
        if (zM157654b || zM157656d) {
            r87.C69821c.m129838g("IMUtils", "disable push：disableBilateralPush=" + zM157654b + ", isInPushIMHoldout=" + zM157656d);
            yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
                @Override
                public final java.lang.Object invoke(java.lang.Object obj) {
                    java.util.HashMap map = new java.util.HashMap(4);
                    map.put("pushReverseABValue", 1);
                    ((ej6.InterfaceC35214c.t0) obj).f122150q = map;
                    return null;
                }
            });
        }
    }

    public static void m78059p(@p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        if (com.kwai.robust.PatchProxy.applyVoidOneRefs(kwaiMsg, null, eff.C35035z0.class, "7")) {
            return;
        }
        final android.app.Activity activityM37978f = com.kwai.framework.activitycontext.ActivityContext.m37970i().m37978f();
        if (!(activityM37978f instanceof com.yxcorp.gifshow.activity.GifshowActivity)) {
            return;
        }
        yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                ej6.InterfaceC35214c.t0 t0Var = (ej6.InterfaceC35214c.t0) obj;
                java.lang.String strP20 = ((com.yxcorp.gifshow.activity.GifshowActivity) activityM37978f).p20();
                if (!com.yxcorp.utility.TextUtils.m71737z(strP20)) {
                    t0Var.f122139f = strP20;
                    return null;
                }
                return null;
            }
        });
    }

    public static java.lang.String m78047d(int i4, java.lang.String str) {
        java.lang.Object objApplyIntObject = com.kwai.robust.PatchProxy.applyIntObject(eff.C35035z0.class, "5", null, i4, str);
        if (objApplyIntObject != com.kwai.robust.PatchProxyResult.class) {
            return (java.lang.String) objApplyIntObject;
        }
        return m78048e("0", i4, str);
    }

    public static void m78052i(@p698w0.InterfaceC82235a final java.lang.String str, @p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        if (com.kwai.robust.PatchProxy.applyVoidTwoRefs(str, kwaiMsg, null, eff.C35035z0.class, "10")) {
            return;
        }
        yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                java.lang.String str2 = str;
                java.util.Map<java.lang.String, byte[]> map = ((ej6.InterfaceC35214c.t0) obj).f122140g;
                if (map != null) {
                    map.remove(str2);
                    return null;
                }
                return null;
            }
        });
    }

    public static void m78056m(@p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg, final boolean z) {
        if (com.kwai.robust.PatchProxy.applyVoidObjectBoolean(eff.C35035z0.class, "12", null, kwaiMsg, z)) {
            return;
        }
        yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                ((ej6.InterfaceC35214c.t0) obj).f122142i = z;
                return null;
            }
        });
    }

    public static byte[] m78046c(@p698w0.InterfaceC82235a final java.lang.String str, com.kwai.imsdk.msg.KwaiMsg kwaiMsg) {
        java.lang.Object objApplyTwoRefs = com.kwai.robust.PatchProxy.applyTwoRefs(str, kwaiMsg, null, eff.C35035z0.class, com.kuaishou.android.model.ads.PhotoAdvertisement.ACTION_BAR_DISPLAY_TYPE_THANOS_SIMPLE_BAR);
        if (objApplyTwoRefs != com.kwai.robust.PatchProxyResult.class) {
            return (byte[]) objApplyTwoRefs;
        }
        return (byte[]) yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                java.lang.String str2 = str;
                ej6.InterfaceC35214c.t0 t0Var = (ej6.InterfaceC35214c.t0) obj;
                if (boi.C5466t.m12827i(t0Var.f122140g)) {
                    return null;
                }
                return t0Var.f122140g.get(str2);
            }
        });
    }

    public static void m78058o(@p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg, java.util.Map<java.lang.String, java.lang.String> map) {
        java.util.Map<java.lang.String, java.lang.String> mapM155523f;
        byte[] bytes = null;
        if (com.kwai.robust.PatchProxy.applyVoidTwoRefs(kwaiMsg, map, null, eff.C35035z0.class, "16") || (mapM155523f = yff.C88931f.m155523f(kwaiMsg)) == null) {
            return;
        }
        mapM155523f.putAll(map);
        try {
            bytes = up8.C78711a.f232648a.m22800r(mapM155523f, java.util.HashMap.class).getBytes();
        } catch (java.lang.Exception e5) {
            r87.C69821c.m129835d("setLocalExtraMap Exception ", mapM155523f.toString(), e5);
        }
        kwaiMsg.setLocalExtra(bytes);
    }

    public static void m78053j(@p698w0.InterfaceC82235a final java.lang.String str, @p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg, final byte[] bArr) {
        if (com.kwai.robust.PatchProxy.applyVoidThreeRefs(str, kwaiMsg, bArr, null, eff.C35035z0.class, "9") || bArr == null) {
            return;
        }
        yff.C88931f.m155519b(kwaiMsg, new txi.InterfaceC76575l() {
            @Override
            public final java.lang.Object invoke(java.lang.Object obj) {
                java.lang.String str2 = str;
                byte[] bArr2 = bArr;
                ej6.InterfaceC35214c.t0 t0Var = (ej6.InterfaceC35214c.t0) obj;
                java.util.Map<java.lang.String, byte[]> map = t0Var.f122140g;
                if (map == null) {
                    map = new java.util.HashMap<>();
                }
                map.put(str2, bArr2);
                t0Var.f122140g = map;
                return null;
            }
        });
    }

    public static void m78057n(@p698w0.InterfaceC82235a com.kwai.imsdk.msg.KwaiMsg kwaiMsg, java.lang.String str, java.lang.String str2) {
        java.util.Map<java.lang.String, java.lang.String> mapM155523f;
        if (com.kwai.robust.PatchProxy.applyVoidThreeRefs(kwaiMsg, str, str2, null, eff.C35035z0.class, "15") || (mapM155523f = yff.C88931f.m155523f(kwaiMsg)) == null) {
            return;
        }
        mapM155523f.put(str, str2);
        byte[] bytes = null;
        try {
            bytes = up8.C78711a.f232648a.m22800r(mapM155523f, java.util.HashMap.class).getBytes();
        } catch (java.lang.Exception e5) {
            r87.C69821c.m129835d("setLocalExtraMap Exception ", mapM155523f.toString(), e5);
        }
        kwaiMsg.setLocalExtra(bytes);
    }

    public static java.lang.String m78048e(java.lang.String str, int i4, java.lang.String str2) {
        int i5;
        boolean zBooleanValue;
        java.lang.Object objApplyObjectIntObject = com.kwai.robust.PatchProxy.applyObjectIntObject(eff.C35035z0.class, "6", null, str, i4, str2);
        if (objApplyObjectIntObject != com.kwai.robust.PatchProxyResult.class) {
            return (java.lang.String) objApplyObjectIntObject;
        }
        if (i4 == 0) {
            return str2;
        }
        if (i4 == 24100) {
            try {
                return new org.json.JSONObject(str2).optString("error_msg");
            } catch (org.json.JSONException e5) {
                r87.C69821c.m129835d("IMUtils", "getErrorMsg", e5);
                return str2;
            }
        }
        if (!android.text.TextUtils.isEmpty(str2)) {
            bgf.C4638p1 c4638p1M11311e = bgf.C4638p1.m11311e(str);
            java.util.Objects.requireNonNull(c4638p1M11311e);
            java.lang.Object objApplyInt = com.kwai.robust.PatchProxy.applyInt(bgf.C4638p1.class, "19", c4638p1M11311e, i4);
            if (objApplyInt != com.kwai.robust.PatchProxyResult.class) {
                zBooleanValue = ((java.lang.Boolean) objApplyInt).booleanValue();
            } else {
                com.kwai.imsdk.C17050m c17050mM40880x = com.kwai.imsdk.C17050m.m40880x(c4638p1M11311e.f14753a);
                java.util.Objects.requireNonNull(c17050mM40880x);
                java.lang.Object objApplyInt2 = com.kwai.robust.PatchProxy.applyInt(com.kwai.imsdk.C17050m.class, "124", c17050mM40880x, i4);
                if (objApplyInt2 != com.kwai.robust.PatchProxyResult.class) {
                    zBooleanValue = ((java.lang.Boolean) objApplyInt2).booleanValue();
                } else {
                    i37.C44674b.m92073i("KwaiIMManager#isErrorValidForShowingErrorMessage", "errorCode: " + i4);
                    f41.InterfaceC36818a.a aVar = com.kwai.imsdk.internal.client.C16970u.m40603m(c17050mM40880x.f54764c).f54473c;
                    if (aVar == null || (i5 = aVar.f126457z) <= 0) {
                        i5 = 20000;
                    }
                    if (i4 >= i5) {
                        zBooleanValue = true;
                    } else {
                        zBooleanValue = false;
                    }
                }
            }
            if (zBooleanValue) {
                return str2;
            }
        }
        if (!com.yxcorp.utility.NetworkUtilsNoLock.m71605d(ib8.C45178a.m92727b())) {
            return bhh.C4875m1.m11827q(2131830521);
        }
        if (i4 != -16417) {
            if (i4 != -16416) {
                if (i4 != -16410) {
                    if (i4 != -16406) {
                        if (i4 != -120 && i4 != -109 && i4 != -108) {
                            return bhh.C4875m1.m11827q(2131824954);
                        }
                    } else {
                        return bhh.C4875m1.m11827q(2131832818);
                    }
                }
                return "";
            }
            return bhh.C4875m1.m11827q(2131824945);
        }
        return bhh.C4875m1.m11827q(2131825620);
    }
}
