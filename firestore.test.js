const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_student_2";
const HOD_UID = "hod_faculty_9";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated access is denied across collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("students").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("attendance").get());
  await assertFails(unauthDb.collection("forumPosts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({ uid: ALICE_UID, fullName: "Alice" }));
});

test("Authenticated user can manage their own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      fullName: "Alice Student",
      role: "student",
      email: "alice@makaut.ac.in"
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user cannot modify another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      uid: BOB_UID,
      fullName: "Bob Student",
      role: "student"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({ fullName: "Hacked" }));
});

test("Forum posts: Authenticated user can create and author can update/delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("forumPosts").doc("post_1").set({
      title: "Data Structures Query",
      content: "How does AVL tree rotation work?",
      authorId: ALICE_UID,
      authorName: "Alice"
    })
  );

  // Bob can read the post
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("forumPosts").doc("post_1").get());

  // Bob cannot delete Alice's post
  await assertFails(bobDb.collection("forumPosts").doc("post_1").delete());

  // Alice can delete her own post
  await assertSucceeds(aliceDb.collection("forumPosts").doc("post_1").delete());
});

test("Student attendance: Student can read their own attendance, cannot create/update", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("attendance").doc("att_1").set({
      studentId: ALICE_UID,
      status: "PRESENT",
      date: "2026-10-19"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("attendance").doc("att_1").get());

  // Alice cannot forge attendance records
  await assertFails(
    aliceDb.collection("attendance").doc("att_fake").set({
      studentId: ALICE_UID,
      status: "PRESENT",
      date: "2026-10-19"
    })
  );
});
