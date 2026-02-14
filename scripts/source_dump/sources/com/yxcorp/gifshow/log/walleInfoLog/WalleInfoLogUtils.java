package com.yxcorp.gifshow.log.walleInfoLog;

public final class WalleInfoLogUtils {

    public static final com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils f95344a = new com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils();

    public static final wwi.InterfaceC85031u f95345b = wwi.C85037w.m150532c(new txi.InterfaceC76564a() {
        @Override
        public final java.lang.Object invoke() {
            boolean zBooleanValue;
            com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils walleInfoLogUtils = com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.f95344a;
            java.lang.Object objApplyWithListener = com.kwai.robust.PatchProxy.applyWithListener(null, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "8");
            if (objApplyWithListener != com.kwai.robust.PatchProxyResult.class) {
                zBooleanValue = ((java.lang.Boolean) objApplyWithListener).booleanValue();
            } else {
                boolean booleanValue = false;
                try {
                    booleanValue = com.kwai.sdk.switchconfig.C19041a.m46842D().getBooleanValue("enableWalleInfoReport", false);
                } catch (java.lang.Exception unused) {
                }
                com.kwai.robust.PatchProxy.onMethodExit(com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "8");
                zBooleanValue = booleanValue;
            }
            return java.lang.Boolean.valueOf(zBooleanValue);
        }
    });

    public static java.util.concurrent.ConcurrentHashMap<java.lang.String, java.util.HashSet<java.lang.String>> f95346c = new java.util.concurrent.ConcurrentHashMap<>();

    @sxi.InterfaceC73876l
    public static final boolean m64352a() {
        java.lang.Object objApply = com.kwai.robust.PatchProxy.apply(null, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, com.kuaishou.android.model.ads.PhotoAdvertisement.COMMENT_ACTIONBAR_STYLE_2);
        return objApply != com.kwai.robust.PatchProxyResult.class ? ((java.lang.Boolean) objApply).booleanValue() : f95344a.m64355b();
    }

    public final boolean m64355b() {
        java.lang.Object objApply = com.kwai.robust.PatchProxy.apply(this, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "1");
        if (objApply == com.kwai.robust.PatchProxyResult.class) {
            objApply = f95345b.getValue();
        }
        return ((java.lang.Boolean) objApply).booleanValue();
    }

    public static final class WalleActivityInfo implements java.io.Serializable {

        @tr.InterfaceC76155c("activityIds")
        public java.util.HashMap<java.lang.String, java.util.HashSet<java.lang.String>> activityIds;

        public final java.util.HashMap<java.lang.String, java.util.HashSet<java.lang.String>> getActivityIds() {
            return this.activityIds;
        }

        public final void setActivityIds(java.util.HashMap<java.lang.String, java.util.HashSet<java.lang.String>> map) {
            this.activityIds = map;
        }
    }

    public static final class RunnableC26500a implements java.lang.Runnable {

        public final boolean f95347b;

        public final java.lang.String f95348c;

        public final java.util.HashSet<java.lang.String> f95349d;

        public final double f95350e;

        public RunnableC26500a(boolean z, java.lang.String str, java.util.HashSet<java.lang.String> hashSet, double d5) {
            this.f95347b = z;
            this.f95348c = str;
            this.f95349d = hashSet;
            this.f95350e = d5;
        }

        @Override
        public final void run() {
            java.util.HashSet<java.lang.String> hashSet;
            if (com.kwai.robust.PatchProxy.applyVoid(this, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.RunnableC26500a.class, "1")) {
                return;
            }
            if (this.f95347b && (hashSet = com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.f95346c.get(this.f95348c)) != null) {
                java.util.HashSet<java.lang.String> hashSet2 = this.f95349d;
                java.lang.String str = this.f95348c;
                if (kotlin.jvm.internal.C51183a.m104775g(hashSet, hashSet2)) {
                    vyg.C82141a.m145634u().mo8271o("WalleInfoLogUtils", "logWalleInfoCustomEvent newId == oldId do not log key = " + str + " ids = " + hashSet2, new java.lang.Object[0]);
                    return;
                }
            }
            com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.f95346c.put(this.f95348c, this.f95349d);
            vyg.C82141a.m145634u().mo8271o("WalleInfoLogUtils", "logWalleInfoCustomEvent ratio =" + this.f95350e + " key = " + this.f95348c + " ids = " + this.f95349d, new java.lang.Object[0]);
            java.util.HashMap<java.lang.String, java.util.HashSet<java.lang.String>> map = new java.util.HashMap<>();
            map.put(this.f95348c, this.f95349d);
            com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo walleActivityInfo = new com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo();
            walleActivityInfo.setActivityIds(map);
            try {
                mze.C57928j2.m114782R("WALLE_ACTIVITY_INFO", up8.C78711a.f232648a.m22799q(walleActivityInfo), 14);
            } catch (java.lang.Exception e5) {
                vyg.C82141a.m145634u().mo761k("WalleInfoLogUtils", "logCustomEvent fail: key = " + this.f95348c + "  activityIds = " + this.f95349d + " ratio = " + this.f95350e, e5);
            }
        }
    }

    public static final class RunnableC26501b implements java.lang.Runnable {

        public final java.lang.String f95351b;

        public final java.util.HashSet<java.lang.String> f95352c;

        public RunnableC26501b(java.lang.String str, java.util.HashSet<java.lang.String> hashSet) {
            this.f95351b = str;
            this.f95352c = hashSet;
        }

        @Override
        public final void run() {
            com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo walleActivityInfo;
            boolean z;
            java.util.HashMap<java.lang.String, java.util.HashSet<java.lang.String>> activityIds;
            if (com.kwai.robust.PatchProxy.applyVoid(this, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.RunnableC26501b.class, "1")) {
                return;
            }
            com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils walleInfoLogUtils = com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.f95344a;
            java.util.Objects.requireNonNull(walleInfoLogUtils);
            java.lang.Object objApply = com.kwai.robust.PatchProxy.apply(walleInfoLogUtils, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "7");
            java.util.HashSet<java.lang.String> hashSet = null;
            if (objApply != com.kwai.robust.PatchProxyResult.class) {
                walleActivityInfo = (com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo) objApply;
            } else {
                java.lang.Object objApplyOneRefs = com.kwai.robust.PatchProxy.applyOneRefs("WALLE_ACTIVITY_INFO", null, wc8.C83029a.class, "4");
                if (objApplyOneRefs == com.kwai.robust.PatchProxyResult.class) {
                    java.util.Map<java.lang.String, java.lang.Object> mapM45351u = com.kwai.performance.stability.crash.monitor.util.C18414f.m45351u();
                    if (mapM45351u != null && mapM45351u.containsKey("WALLE_ACTIVITY_INFO")) {
                        objApplyOneRefs = mapM45351u.get("WALLE_ACTIVITY_INFO");
                    } else {
                        objApplyOneRefs = null;
                    }
                }
                if (objApplyOneRefs instanceof com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo) {
                    walleActivityInfo = (com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo) objApplyOneRefs;
                } else {
                    walleActivityInfo = null;
                }
            }
            if (walleActivityInfo != null && (activityIds = walleActivityInfo.getActivityIds()) != null) {
                hashSet = activityIds.get(this.f95351b);
            }
            java.util.HashSet<java.lang.String> hashSet2 = this.f95352c;
            boolean z4 = true;
            if (hashSet2 != null && !hashSet2.isEmpty()) {
                z = false;
            } else {
                z = true;
            }
            if (z) {
                if (hashSet != null && !hashSet.isEmpty()) {
                    z4 = false;
                }
                if (z4) {
                    return;
                }
            }
            if (hashSet != null) {
                java.util.HashSet<java.lang.String> hashSet3 = this.f95352c;
                java.lang.String str = this.f95351b;
                if (kotlin.jvm.internal.C51183a.m104775g(hashSet, hashSet3)) {
                    vyg.C82141a.m145634u().mo8271o("WalleInfoLogUtils", "updateWalleInfoExceptionEvent key = " + str + "  ids == oldIds " + hashSet3, new java.lang.Object[0]);
                    return;
                }
            }
            vyg.C82141a.m145634u().mo8271o("WalleInfoLogUtils", "updateWalleInfoExceptionEvent key = " + this.f95351b + "  ids = " + this.f95352c, new java.lang.Object[0]);
            if (walleActivityInfo == null) {
                walleActivityInfo = new com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.WalleActivityInfo();
            }
            java.util.HashMap<java.lang.String, java.util.HashSet<java.lang.String>> activityIds2 = walleActivityInfo.getActivityIds();
            if (activityIds2 == null) {
                activityIds2 = new java.util.HashMap<>();
            }
            activityIds2.put(this.f95351b, this.f95352c);
            walleActivityInfo.setActivityIds(activityIds2);
            wc8.C83029a.m147072e("WALLE_ACTIVITY_INFO", walleActivityInfo);
        }
    }

    public final void m64357f(java.lang.String str, java.util.HashSet<java.lang.String> hashSet) {
        m0f.C55413a c55413a;
        if (com.kwai.robust.PatchProxy.applyVoidTwoRefs(str, hashSet, this, com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "5")) {
            return;
        }
        com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.RunnableC26501b runnable = new com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.RunnableC26501b(str, hashSet);
        if (!com.kwai.robust.PatchProxy.applyVoidOneRefs(runnable, null, m0f.C55414b.class, com.kuaishou.android.model.ads.PhotoAdvertisement.COMMENT_ACTIONBAR_STYLE_2)) {
            kotlin.jvm.internal.C51183a.m104784p(runnable, "runnable");
            m0f.C55414b c55414b = m0f.C55414b.f173173a;
            java.util.Objects.requireNonNull(c55414b);
            java.lang.Object objApply = com.kwai.robust.PatchProxy.apply(c55414b, m0f.C55414b.class, "4");
            if (objApply != com.kwai.robust.PatchProxyResult.class) {
                c55413a = (m0f.C55413a) objApply;
            } else {
                if (m0f.C55414b.f173175c == null) {
                    synchronized (c55414b) {
                        if (m0f.C55414b.f173175c == null) {
                            m0f.C55414b.f173175c = new m0f.C55413a("walle_info_exception_event", 10);
                        }
                        wwi.C85021q1 c85021q1 = wwi.C85021q1.f248785a;
                    }
                }
                c55413a = m0f.C55414b.f173175c;
            }
            if (c55413a != null) {
                c55413a.m111828c(runnable);
            }
        }
    }

    @sxi.InterfaceC73876l
    public static final void m64354e(java.lang.String key, java.util.HashSet<java.lang.String> hashSet, boolean z) {
        if (com.kwai.robust.PatchProxy.applyVoidObjectObjectBoolean(com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "3", null, key, hashSet, z)) {
            return;
        }
        kotlin.jvm.internal.C51183a.m104784p(key, "key");
        com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils walleInfoLogUtils = f95344a;
        if (!walleInfoLogUtils.m64355b()) {
            return;
        }
        walleInfoLogUtils.m64357f(key, hashSet);
        walleInfoLogUtils.m64356c(z, key, hashSet);
    }

    @sxi.InterfaceC73876l
    public static final void m64353d(java.lang.String key, java.lang.String str, boolean z) {
        if (com.kwai.robust.PatchProxy.applyVoidObjectObjectBoolean(com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "4", null, key, str, z)) {
            return;
        }
        kotlin.jvm.internal.C51183a.m104784p(key, "key");
        com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils walleInfoLogUtils = f95344a;
        if (!walleInfoLogUtils.m64355b()) {
            return;
        }
        java.util.HashSet<java.lang.String> hashSet = new java.util.HashSet<>();
        if (str != null) {
            hashSet.add(str);
        }
        walleInfoLogUtils.m64357f(key, hashSet);
        walleInfoLogUtils.m64356c(z, key, hashSet);
    }

    public final void m64356c(boolean z, java.lang.String str, java.util.HashSet<java.lang.String> hashSet) {
        boolean z4;
        m0f.C55413a c55413a;
        if (com.kwai.robust.PatchProxy.applyVoidBooleanObjectObject(com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.class, "6", this, z, str, hashSet)) {
            return;
        }
        if (hashSet != null && !hashSet.isEmpty()) {
            z4 = false;
        } else {
            z4 = true;
        }
        if (z4) {
            return;
        }
        double dM145e = a0f.C0053h.m145e("customEvent", "WALLE_ACTIVITY_INFO");
        if (java.lang.Math.random() > dM145e) {
            return;
        }
        com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.RunnableC26500a runnable = new com.yxcorp.gifshow.log.walleInfoLog.WalleInfoLogUtils.RunnableC26500a(z, str, hashSet, dM145e);
        if (!com.kwai.robust.PatchProxy.applyVoidOneRefs(runnable, null, m0f.C55414b.class, "1")) {
            kotlin.jvm.internal.C51183a.m104784p(runnable, "runnable");
            m0f.C55414b c55414b = m0f.C55414b.f173173a;
            java.util.Objects.requireNonNull(c55414b);
            java.lang.Object objApply = com.kwai.robust.PatchProxy.apply(c55414b, m0f.C55414b.class, "3");
            if (objApply != com.kwai.robust.PatchProxyResult.class) {
                c55413a = (m0f.C55413a) objApply;
            } else {
                if (m0f.C55414b.f173174b == null) {
                    synchronized (c55414b) {
                        if (m0f.C55414b.f173174b == null) {
                            m0f.C55414b.f173174b = new m0f.C55413a("walle_info_custom_event", 10);
                        }
                        wwi.C85021q1 c85021q1 = wwi.C85021q1.f248785a;
                    }
                }
                c55413a = m0f.C55414b.f173174b;
            }
            if (c55413a != null) {
                c55413a.m111828c(runnable);
            }
        }
    }
}
