import { Avatar, AvatarBadge, AvatarFallback, AvatarImage } from "@/components/ui/avatar"

type AvatarPhotoProps = {
  name: string
  src?: string
  online?: boolean
  size?: "default" | "sm" | "lg"
  className?: string
}

function initials(name: string) {
  return name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase()
}

export function AvatarPhoto({ name, src, online, size = "default", className }: AvatarPhotoProps) {
  return (
    <Avatar size={size} className={className}>
      {src ? <AvatarImage src={src} alt={`${name} avatar`} /> : null}
      <AvatarFallback>{initials(name)}</AvatarFallback>
      {online ? <AvatarBadge className="bg-primary ring-background" /> : null}
    </Avatar>
  )
}
