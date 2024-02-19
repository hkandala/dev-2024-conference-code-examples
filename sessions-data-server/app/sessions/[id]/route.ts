import sessions from "../../../data/sessions.json";

export async function GET(
  _request: Request,
  { params }: { params: { id: number } }
) {
  const session = sessions.find((session) => session.id == params.id);
  if (!session) return new Response("Not found", { status: 404 });
  return new Response(session.title + " " + session.abstract, { status: 200 });
}
